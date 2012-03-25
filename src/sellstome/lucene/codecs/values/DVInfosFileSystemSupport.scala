package sellstome.lucene.codecs.values

import org.apache.lucene.store.{IOContext, IndexOutput, Directory}
import org.apache.lucene.util.IOUtils
import sellstome.control.trysupress


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
trait DVInfosFileSystemSupport extends DVSliceFilesSupport {
  //this class factor out all logic that operates on File System
  this: DocValuesSliceInfos =>
  /** This field being set only after prepareCommit phase is finished */
  var pendingInfosOutput: IndexOutput = null

  /** Writes a given slice infos to a directory. */
  protected def write(docValuesId: String, dir: Directory) {
    val infosFileName = fileNameFromGeneration(docValuesId, nextGeneration() )
    incGeneration()
    try {
      val infosWriter: DocValuesSliceInfosWriter = newWriter()
      pendingInfosOutput = infosWriter.writeInfos(dir, infosFileName, this, IOContext.DEFAULT)
      infosWriter.prepareCommit(pendingInfosOutput)
    } catch {
      case e: Throwable => {
        IOUtils.closeWhileHandlingException(pendingInfosOutput)
        trysupress { dir.deleteFile(infosFileName) }
        throw e
      }
    }
  }

  /** Extension point for other writer implementations */
  protected def newWriter(): DocValuesSliceInfosWriter = new DVSliceInfosWriterImpl()

}