package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues.Type
import sellstome.lucene.codecs.DocValuesSlicesSupport
import org.apache.lucene.store.{IndexOutput, IOContext, Directory}
import org.apache.lucene.codecs.lucene40.values.Ints
import sellstome.control.using
import org.apache.lucene.util.{IOUtils, CodecUtil, Counter}
import org.apache.lucene.codecs.DocValuesConsumer
import org.apache.lucene.index.IndexableField

/**
 * Stores ints packed and fixed with fixed-bit precision.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param _dir provides access to a flat list of files
 * @param _docValuesId an unique identifier for a given doc value field
 * @param codecName a name of a given codec
 * @param version a current codec version
 * @param bytesUsed
 * @param context io context
 */
class MutableIntsDVConsumer(_dir: Directory,
                            _docValuesId: String,
                            codecName: String,
                            version: Int,
                            bytesUsed: Counter,
                            context: IOContext,
                            valueType: Type) extends DocValuesConsumer
                                             with DocValuesSlicesSupport {

  def this(dir: Directory, docValuesId: String, bytesUsed: Counter, context: IOContext, valueType: Type) =
    this(dir, docValuesId, Ints.CODEC_NAME, Ints.VERSION_CURRENT, bytesUsed, context, valueType)


  override def add(docID: Int, value: IndexableField) {

  }

  /**
   * write a processed values to a disk.
   * todo zhugrov - remove the body of this method as soon as you
   * can call a super method.
   */
  override def finish(docCount: Int) {
    flushSlicesInfos()
  }

  /** Creates a output for a current writer */
  protected def getOrCreateDataOut(): IndexOutput = using(_dir.createOutput(currentWriteSliceFileName(_docValuesId), context)) {
      out =>
        CodecUtil.writeHeader(out, codecName, version)
        out
  }

  protected def getType: Type = valueType

  protected def docValuesId() = _docValuesId

  protected def dir()         = _dir
}
