package sellstome.lucene.codecs

import scala.collection.JavaConversions._
import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.store.Directory
import java.io.IOException
import org.apache.lucene.util.IOUtils
import java.util.{HashSet, Set}
import org.apache.lucene.index._
import org.apache.lucene.codecs.{PerDocProducerBase, DocValuesConsumer, PerDocConsumer}
import values.MutableDocValuesAware

object MutableDocValuesPerDocConsumer {

  def files(info: SegmentInfo, files: Set[String], extension: String) {
    this.files(info.dir, info.getFieldInfos(), info.name, files, extension)
  }

  /**
   * Calculates a set of files that used for storing docValues on the file system.
   * @param dir abstract access to a underlying storage
   * @param fieldInfos provides access to a FieldInfo segment's file.
   * @param segmentName a name of a given lucene segment
   * @param files a mutable set that being populated with files where doc values stored
   * @param extension a doc values files extension. We don't use a compound file format for this codec.
   */
  protected def files(dir: Directory, fieldInfos: FieldInfos, segmentName: String, files: Set[String], extension: String) {
    for (fieldInfo <- fieldInfos) {
      if (fieldInfo.hasDocValues()) {
        var filename: String = docValuesId(segmentName, fieldInfo.number)
        files.add(IndexFileNames.segmentFileName(filename, "", extension))
        try {
          assert(dir.fileExists(IndexFileNames.segmentFileName(filename, "", extension)))
        } catch {
          case e: IOException => {
            throw new RuntimeException(e)
          }
        }
      }
    }
  }

  protected def docValuesId(segmentsName: String, fieldId: Int): String = {
    return segmentsName + "_" + fieldId
  }

}

/**
 * Consumes docValues values.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesPerDocConsumer(state: PerDocWriteState, fileExtension: String) extends PerDocConsumer
                                                                                     with MutableDocValuesAware {

  /**
   * Adds a new DocValuesField
   * @throws UnsupportedOperationException in case if does not supports a given doc values type.
   */
  def addValuesField(docValuesType: Type, field: FieldInfo): DocValuesConsumer = {
    if (!isSupportedType(docValuesType)) throw new UnsupportedOperationException("Codec doesn't support given type: "+docValuesType)
    return MutableDocValuesConsumerFactory.create(docValuesType, PerDocProducerBase.docValuesId(state.segmentName, field.number),
      state.directory, state.bytesUsed, state.context)
  }

  /** delete all files that contain a un-committed data */
  def abort() {
    val fileSet = new HashSet[String]
    MutableDocValuesPerDocConsumer.files(state.directory, state.fieldInfos, state.segmentName, fileSet, fileExtension)
    IOUtils.deleteFilesIgnoringExceptions(state.directory, fileSet.toSeq: _*)
  }

  def close() {
    //need nothing to do here
  }

}
