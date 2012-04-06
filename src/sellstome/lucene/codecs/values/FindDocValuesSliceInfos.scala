package sellstome.lucene.codecs.values

import java.io.IOException
import collection.mutable.HashSet
import org.apache.lucene.index.{IndexNotFoundException, CorruptIndexException}
import javax.annotation.Nonnull
import org.apache.lucene.store.{IOContext, Directory}
import sellstome.util.Logging
import java.io.FileNotFoundException

object FindDocValuesSliceInfos {
  import FindDVSlicesGenMethod._

  val DefaultGenLookaheadCount = 10
  /** Encapsulates a progress info records */
  class ProgressInfo[T] {
    /** last seen exception */
    protected var lastSeenExc: Throwable = null
    protected var result: Option[T] = None
    protected var retryCount: Int = 0
    /** number of attempts of using a particular methods */
    protected var genLookaheadCount: Int = 1
    protected val defaultGenLookaheadCount: Int = 10
    /** Size of the #genSeen set in previous iteration */
    protected var genSeenPrevSize = 0
    protected val genSeen = new HashSet[Long]
    /** the method used for resolving of this */
    protected var useMethod: FindDVSlicesGenMethod = FindDVSlicesGenMethod.FileSystem

    /**
     * Determines if should try again
     * @return if we should try again in a loop
     */
    def isShouldTryAgain(): Boolean = {
      if (result.isDefined) {
          return false
      } else {
        if (useMethod == FindDVSlicesGenMethod.FileSystem) {
          return true
        } else {
          if (genLookaheadCount < defaultGenLookaheadCount) {
            return true
          } else {
            return false
          }
        }
      }
    }

    def proposeMethod(): FindDVSlicesGenMethod = {
      if (useMethod == FindDVSlicesGenMethod.FileSystem) {
        if (!isSeenProgress() && retryCount >= 2) {
          useMethod = FindDVSlicesGenMethod.LookAhead
        }
        return useMethod
      } else {
        useMethod = FindDVSlicesGenMethod.LookAhead
        return useMethod
      }
    }

    /**
     * a next try
     * @throws IllegalStateException if we use not supported find dv gen method
     * @throws Throwable in case if we should stop the further execution. Where the actual exception is the last seen exception.
     */
    def advance() {
      if (useMethod == FindDVSlicesGenMethod.FileSystem) {
        if (isSeenProgress()) {
          retryCount = 0
        } else {
          retryCount = retryCount + 1
        }
      } else if (useMethod == FindDVSlicesGenMethod.LookAhead) {
        genLookaheadCount = genLookaheadCount + 1
      } else {
        throw new IllegalStateException("not supported find dv gen method: %s".format(useMethod))
      }
    }

    def addGenSeen(gen: Long) {
      genSeenPrevSize = genSeen.size
      genSeen.add(gen)
    }

    /** Populates a last seen exception. */
    def setLastSeenException(@Nonnull exc: Throwable) { this.lastSeenExc = exc }

    def setResult(@Nonnull result: T) { this.result = Some(result) }

    /** @throws Predef.NoSuchElementException if no result is set. */
    def getResult(): T = {
      if (result.isDefined) {
        return result.get
      } else {
        if (lastSeenExc != null) {
          throw lastSeenExc
        } else {
          throw new IllegalStateException("An invalid workflow branch. Need investigate")
        }
      }
    }

    /**
     * Gets a gen by simply advancing the last seen generation
     * @return
     */
    def genByLookaheadMethod(): Long = genSeen.max + genLookaheadCount

    /** whenever we receive a progressive values for slices generation */
    protected def isSeenProgress(): Boolean = genSeenPrevSize != genSeen.size

  }
}

/**
 * Utility class for executing code that needs to do
 * something with the current slices file.  This is
 * necessary with lock-less commits because from the time
 * you locate the current slices file name, until you
 * actually open it, read its contents, or check modified
 * time, etc., it could have been deleted due to a writer
 * commit finishing.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param docValuesId a unique identifier for a given dv field
 * @param dir provides access to a flat list of files
 */
class FindDocValuesSliceInfos(docValuesId: String, dir: Directory) extends DVInfosFilenamesSupport
                                                                   with Logging {
  import FindDocValuesSliceInfos.ProgressInfo

  /**
   * Finds a given slices file and pass its to a
   * @param process function that proccess a most resent infos file
   * @tparam T the type of an object of the proccess function
   * @return object returned by the proccess function or throws exception if could not find any.
   */
  @throws(classOf[CorruptIndexException])
  @throws(classOf[IOException])
  def find[T](process: String => T): T = {
    val progressInfo = new ProgressInfo[T]()

    while(progressInfo.isShouldTryAgain()) {
      val method = progressInfo.proposeMethod()
      if (method == FindDVSlicesGenMethod.FileSystem) {
        val genOrNone: Option[Long] = readLastGen(docValuesId, dir)
        if (genOrNone.isEmpty) {
          throw new IndexNotFoundException("No docValueId.dvslices* file found in %s.".format(dir))
        } else {
          progressInfo.addGenSeen(genOrNone.get)
          val docValuesSliceFN = fileForGeneration(docValuesId, genOrNone.get)
          try {
            progressInfo.setResult(process(docValuesSliceFN))
          } catch {
            case e: IOException => {
              progressInfo.setLastSeenException(e)
            }
          }
        }
      } else if (method == FindDVSlicesGenMethod.LookAhead) {
        try {
            val docValuesSliceFN = fileForGeneration(docValuesId, progressInfo.genByLookaheadMethod())
            progressInfo.setResult(process(docValuesSliceFN))
        } catch {
          case e: IOException => {
            progressInfo.setLastSeenException(e)
          }
        }
      } else { throw new IllegalStateException("not supported find dv gen method %s".format(method)) }
      progressInfo.advance()
    }

    return progressInfo.getResult()
  }

  /**
   * In we couldn't find a slices info file we should check that we don't have any other files related to this doc values id.
   * In either words we define here a index state invariant
   * @param docValuesId a unique identifier for a given field && segment combination
   * @param dir provides access to a flat list of files
   * @return if a given doc values field values for a given segment are in consistent state
   */
  protected def isIndexCorrupted(@Nonnull docValuesId: String, @Nonnull dir: Directory): Boolean = {

    ???
  }

  /**
   * Tries to read the last commit generation from FS.
   * @param docValuesId a base name for a given doc values slices
   * @param dir access to a flat list of files
   * @return a last commit generation
   */
  protected def readLastGen(@Nonnull docValuesId: String, @Nonnull dir: Directory): Option[Long] = {
    val files = dir.listAll()
    val genA = if (files != null) getLastCommitGeneration(docValuesId, files) else None
    val genB = readLastGenFromGenFile(docValuesId, dir)
    return List(genA, genB).foldLeft[Option[Long]](None) {
      (maxGenOrNone, genOrNone) =>
        maxGenOrNone match {
          case Some(maxGen) => genOrNone match {
            case Some(gen) => if (gen > maxGen) genOrNone else maxGenOrNone
            case None      => maxGenOrNone
          }
          case None => genOrNone
        }
    }
  }

  /**
   * Also open docValuesId.dvslices_gen and read its
   * contents.
   * @param docValuesId base name for a given doc values slices
   * @param dir provides access to a flat list of files
   * @return a commit generation recorded in the docValuesId.dvslices_gen
   */
  protected def readLastGenFromGenFile(docValuesId: String, dir: Directory): Option[Long] = {
    (try {
      Some(dir.openInput(generationFile(docValuesId), IOContext.READONCE))
    } catch {
      case e: FileNotFoundException =>  { error(e); None }
      case e: IOException =>            { error(e); None }
    }).map[Option[Long]]( (genInput) =>
      try {
        val gen0: Long = genInput.readLong()
        val gen1: Long = genInput.readLong()
        debug("fallback check: %s; %s".format(gen0, gen1))
        if (gen0 == gen1) {
          Some(gen0)
        } else {
          None
        }
      } catch {
        case e: CorruptIndexException => { throw e }
        case e: IOException => { error(e); None }
      } finally {
        genInput.close()
      }
    ).flatMap[Long]( optionGen => if (optionGen.isDefined) Some(optionGen.get) else None)
  }

}

/** Possible ways for resolving for doc slices generation */
protected object FindDVSlicesGenMethod extends Enumeration {
  /** types for enum constants */
  type FindDVSlicesGenMethod = Value

  val FileSystem, LookAhead = Value
}