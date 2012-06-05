package sellstome.lucene.codecs

import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.index._
import org.apache.lucene.codecs.{PerDocProducerBase, DocValuesConsumer, PerDocConsumer}
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
  def addValuesField(dvType: Type, field: FieldInfo): DocValuesConsumer = {
    if (!isSupportedType(dvType)) throw new UnsupportedOperationException("Codec doesn't support a given type: "+dvType)
    return consumerFactory.create(dvType, PerDocProducerBase.docValuesId(state.segmentInfo.name, field.number),
      state.directory, state.bytesUsed, state.context)
  }

  /** delete all files that contain a un-committed data */
  def abort() {
    //val fileSet = new HashSet[String]
    //files(state.directory, state.fieldInfos, state.segmentName, fileSet)
    //IOUtils.deleteFilesIgnoringExceptions(state.directory, fileSet.toSeq: _*)
    //todo zhugrov a - investigate should we delete files here.
  }

  def close() {
    //need nothing to do here
  }

}