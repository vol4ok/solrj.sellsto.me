package sellstome.lucene.codecs

import scala.collection.JavaConversions._
import org.apache.lucene.index.DocValues.Type
import java.util.{HashSet, Set}
import org.apache.lucene.index._
import org.apache.lucene.codecs.{PerDocProducerBase, DocValuesConsumer, PerDocConsumer}
import org.apache.lucene.util.IOUtils
import org.apache.lucene.store.Directory
import values.{MutableDocValuesUtils, DocValuesUtils, MutableDocValuesAware}

/**
 * Consumes docValues values.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesPerDocConsumer(state: PerDocWriteState) extends PerDocConsumer with MutableDocValuesAware {
  /** a factory for creating a new doc values consumer */
  protected val consumerFactory: DocValuesConsumerFactory = MutableDocValuesConsumerFactory

  /** a set of utility functions for working with doc values */
  protected val dvUtils: DocValuesUtils = MutableDocValuesUtils

  /**
   * Adds a new DocValuesField
   * @throws UnsupportedOperationException in case if does not supports a given doc values type.
   */
  def addValuesField(docValuesType: Type, field: FieldInfo): DocValuesConsumer = {
    if (!isSupportedType(docValuesType)) throw new UnsupportedOperationException("Codec doesn't support given type: "+docValuesType)
    return consumerFactory.create(docValuesType, PerDocProducerBase.docValuesId(state.segmentInfo.name, field.number),
      state.directory, state.bytesUsed, state.context)
  }

  /** delete all files that contain a un-committed data */
  def abort() {
    //val fileSet = new HashSet[String]
    //files(state.directory, state.fieldInfos, state.segmentName, fileSet)
    //IOUtils.deleteFilesIgnoringExceptions(state.directory, fileSet.toSeq: _*)
  }

  def close() {
    //need nothing to do here
  }

  /**
   * Calculates a set of files that used for storing docValues on the file system.
   * todo zhugrov a - this method completely wrong
   * @param dir abstract access to a underlying storage
   * @param fieldInfos provides access to a FieldInfo segment's file.
   * @param segmentName a name of a given lucene segment
   * @param files a mutable set that being populated with files where doc values stored
   */
  protected def files(dir: Directory, fieldInfos: FieldInfos, segmentName: String, files: Set[String]) {
    files.addAll(dvUtils.files(dir, fieldInfos, segmentName))
  }

}
