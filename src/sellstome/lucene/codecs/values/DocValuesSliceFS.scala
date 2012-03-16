package sellstome.lucene.codecs.values

import com.google.common.io.Files
import org.apache.commons.io.FilenameUtils
import javax.annotation.{Nullable, Nonnull}
import org.apache.lucene.store.{IOContext, IndexInput, Directory}
import org.apache.lucene.index.{CorruptIndexException, IndexFormatTooNewException, SegmentInfo, IndexFileNames}
import sellstome.util.Logging
import java.io.{IOException, FileNotFoundException}

object DocValuesSliceFS {
  /** Name of the doc values slices file extension */
  val DVSlicesExtension = "dvslices"
  /** Suffix for the DV slices gen file */
  val GenSuffix = "gen"
  /** Name of the extension for the generation reference file */
  val DVSlicesGenExtension = DVSlicesExtension+"_"+GenSuffix
  /**
   * Used for the dv slices gen file only
   * Whenever you add a new format, make it 1 smaller (negative version logic)!
   */
  val FormatSegmentsGenCurrent = -2
}


/**
 * Contains common operations for working
 * on doc values slices on a File System.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DocValuesSliceFS extends Logging {

  /**
   * Tries to read the last commit generation from FS.
   * @param docValuesId a base name for a given doc values slices
   * @param dir access to a flat list of files
   * @return a last commit generation
   */
  def readLastGen(@Nonnull docValuesId: String, @Nonnull dir: Directory): Option[Long] = {
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
   * Get the generation of the most recent changes to a given doc values slices
   * Q: should you allows null as the input parameter?
   * @param files array of file names to check
   * @return a generation number
   */
  def getLastCommitGeneration(@Nonnull docValuesId: String, @Nonnull files: Array[String]): Option[Long] = {
    return files.foldLeft[Option[Long]](None) {
      (maxGenOrNone, fileName) =>
        if (isDocSlicesFileWithId(fileName, docValuesId)) {
          val gen: Long = genForFileName(fileName)
          maxGenOrNone match {
            case Some(maxGen) => if (gen > maxGen) Some(gen) else maxGenOrNone
            case None         => Some(gen)
          }
        } else {
          maxGenOrNone
        }
    }
  }

  /** Parses the generation off the segments file name and return it. */
  def genForFileName(@Nonnull fileName: String): Long = {
    val extension = FilenameUtils.getExtension(fileName)
    if (extension.startsWith(DocValuesSliceFS.DVSlicesExtension)) {
      return extension.substring((DocValuesSliceFS.DVSlicesExtension+"_").length()).toLong
    } else {
      throw new IllegalArgumentException("fileName %s is not a doc values slices file".format(fileName))
    }
  }

  /**
   * Computes a full file name from base, extension and generation.
   */
  def fileNameFromGeneration(@Nonnull base: String, @Nonnull ext: String, @Nonnull gen: Long): String = {
    if (gen == SegmentInfo.WITHOUT_GEN) {
       return base+"."+ext
    } else {
       return base+"."+ext+"_"+gen.toString
    }
  }

  @inline
  protected def isDocSlicesFileWithId(@Nonnull fileName: String, @Nonnull docValuesId: String): Boolean = {
    val fileBase  = FilenameUtils.getBaseName(fileName)
    if (fileBase == docValuesId) {
      val extension = FilenameUtils.getExtension(fileName)
      return extension.contains(DocValuesSliceFS.DVSlicesExtension) && extension != DocValuesSliceFS.DVSlicesGenExtension
    } else {
      return false
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
      Some(dir.openInput(docValuesId+"."+DocValuesSliceFS.DVSlicesGenExtension, IOContext.READONCE))
    } catch {
      case e: FileNotFoundException =>  { error(e); None }
      case e: IOException =>            { error(e); None }
    }).map[Option[Long]]( (genInput) =>
      try {
        val version: Int = genInput.readInt()
        if (version == DocValuesSliceFS.FormatSegmentsGenCurrent) {
          val gen0: Long = genInput.readLong()
          val gen1: Long = genInput.readLong()
          debug("fallback check: %s; %s".format(gen0, gen1))
          if (gen0 == gen1) {
            Some(gen0)
          } else {
            None
          }
        } else {
          throw new IndexFormatTooNewException(genInput, version, DocValuesSliceFS.FormatSegmentsGenCurrent, DocValuesSliceFS.FormatSegmentsGenCurrent)
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