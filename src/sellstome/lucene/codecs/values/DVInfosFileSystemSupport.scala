package sellstome.lucene.codecs.values

import org.apache.lucene.util.IOUtils
import sellstome.control.trysuppress
import org.apache.lucene.store.{ChecksumIndexInput, IOContext, IndexOutput, Directory}
import org.apache.lucene.index.CorruptIndexException
import sellstome.transactional.TwoPhaseCommit
import java.util.Collections
import sellstome.util.Logging
import javax.annotation.Nonnull


object DVInfosFileSystemSupport {
  /** A default generation for slice infos */
  val DefaultGeneration: Long = 0
}


/**
 * Contains common operations for working
 * on doc values slices on a File System.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DVInfosFileSystemSupport extends DVInfosFilenamesSupport
                               with TwoPhaseCommit[Directory]
                               with Logging {
  //this class factor out all logic that operates on File System
  this: DocValuesSliceInfos =>
  /** This field being set only after prepareCommit phase is finished */
  var pendingInfosOutput: IndexOutput = null

  /** Reads the information from the file system */
  def read(dir: Directory) {
    new FindDocValuesSliceInfos(getDocValuesId(), dir).find[Unit]( (infosFileName) => {
      val snapShot = currentSnapshot()
      val infosReader = newReader()
      var input: ChecksumIndexInput = null
      try {
        input = new ChecksumIndexInput(dir.openInput(infosFileName, IOContext.READ))
        infosReader.read(dir, infosFileName, input, this, IOContext.READ)
        val checksumNow  = input.getChecksum()
        val checksumThen = input.readLong()
        if (checksumNow != checksumThen) {
          throw new CorruptIndexException("checksum mismatch in infos file %s".format(infosFileName))
        }
      } catch {
        case e: Throwable => {
          this.resetState(snapShot)
          error(e)
        }
      } finally {
        IOUtils.close(input)
      }
    })
  }

  def prepareCommit(dir: Directory) {
    if (pendingInfosOutput != null) throw new IllegalStateException("An another commit operation seems to be in-progress")
    write(getDocValuesId(), dir)
  }

  def commit(dir: Directory) {
    if (pendingInfosOutput == null) throw new IllegalStateException("prepareCommit was not called")

    try {
      newWriter().finishCommit(pendingInfosOutput)
      pendingInfosOutput = null
    } catch {
      case e: Throwable => {
        rollbackCommit(dir)
        throw e
      }
    }

    sync(getDocValuesId(), dir)
    writeGenFile(getDocValuesId(), dir)
  }

  def rollbackCommit(dir: Directory) {
    if (pendingInfosOutput != null) {
      trysuppress { pendingInfosOutput.close() }
      trysuppress { dir.deleteFile(fileForGeneration(getDocValuesId(), currentGeneration())) }
      pendingInfosOutput = null
    }
  }

  /**
   * Ensure that any writes to these files are moved to
   * stable storage.
   * @throws exception if could not sync with file system
   */
  protected def sync(docValuesId: String, dir: Directory) {
    val infosFileName = fileForGeneration(docValuesId, currentGeneration())
    try {
      dir.sync(Collections.singleton(infosFileName))
    } catch {
      case e: Throwable => {
        trysuppress { dir.deleteFile(infosFileName) }
        throw e
      }
    }
  }

  /** Writes a given slice infos to a directory. */
  protected def write(docValuesId: String, dir: Directory) {
    val infosFileName = fileForGeneration(docValuesId, nextGeneration() )
    incGeneration()
    try {
      val infosWriter: DocValuesSliceInfosWriter = newWriter()
      pendingInfosOutput = infosWriter.writeInfos(dir, infosFileName, this, IOContext.DEFAULT)
      infosWriter.prepareCommit(pendingInfosOutput)
    } catch {
      case e: Throwable => {
        IOUtils.closeWhileHandlingException(pendingInfosOutput)
        trysuppress { dir.deleteFile(infosFileName) }
        throw e
      }
    }
  }

  /**
   * Writes a file that contains a current generation value.
   * If its fails to write a given file it tries to delete it. This method should not
   * throw any exceptions even if an operation fails.
   * @param docValuesId an unique id for a given segment and field
   * @param dir provides access to a flat list of files.
   */
  protected def writeGenFile(@Nonnull docValuesId: String, @Nonnull dir: Directory) {
    try {
      val genOutput = dir.createOutput(generationFile(docValuesId), IOContext.READONCE)
      try {
        genOutput.writeLong(currentGeneration())
        genOutput.writeLong(currentGeneration())
      } finally {
        genOutput.close()
        dir.sync(Collections.singleton(generationFile(docValuesId)))
      }
    } catch {
      case e: Throwable => {
        trysuppress { dir.deleteFile(generationFile(docValuesId)) }
        error(e)
      }
    }
  }

  /** Extension point for other writer implementations */
  protected def newWriter(): DocValuesSliceInfosWriter = DVSliceInfosWriterImpl
  /** Extension point for other reader implementations */
  protected def newReader(): DocValuesSliceInfosReader = DVSliceInfosReaderImpl

}