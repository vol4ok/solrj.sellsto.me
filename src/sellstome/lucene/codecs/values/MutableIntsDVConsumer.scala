package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.index.DocValues.Type._
import sellstome.lucene.codecs.DocValuesSlicesSupport
import org.apache.lucene.store.{IndexOutput, IOContext, Directory}
import org.apache.lucene.codecs.lucene40.values.Ints
import sellstome.control.using
import org.apache.lucene.util.{CodecUtil, Counter}
import org.apache.lucene.codecs.DocValuesConsumer
import org.apache.lucene.index.IndexableField
import sellstome.lucene.io.packed
import packed.array._

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
                            dvType: Type) extends DocValuesConsumer
                                             with DocValuesSlicesSupport {

  def this(dir: Directory, docValuesId: String, bytesUsed: Counter, context: IOContext, valueType: Type) =
    this(dir, docValuesId, Ints.CODEC_NAME, Ints.VERSION_CURRENT, bytesUsed, context, valueType)

  protected lazy val byteWriter   = new PackedArrayWriter(ByteType)
  protected lazy val shortWriter  = new PackedArrayWriter(ShortType)
  protected lazy val intWriter    = new PackedArrayWriter(IntType)
  protected lazy val longWriter   = new PackedArrayWriter(LongType)
  protected lazy val floatWriter  = new PackedArrayWriter(FloatType)
  protected lazy val doubleWriter = new PackedArrayWriter(DoubleType)


  override def add(docID: Int, value: IndexableField) {
    dvType match {
      case FIXED_INTS_8  => byteWriter.add(docID,   value.numericValue().byteValue())
      case FIXED_INTS_16 => shortWriter.add(docID,  value.numericValue().shortValue())
      case FIXED_INTS_32 => intWriter.add(docID,    value.numericValue().intValue())
      case FIXED_INTS_64 => longWriter.add(docID,   value.numericValue().longValue())
      case FLOAT_32      => floatWriter.add(docID,  value.numericValue().floatValue())
      case FLOAT_64      => doubleWriter.add(docID, value.numericValue().doubleValue())
    }
  }

  /**
   * write a processed values to a disk.
   * todo zhugrov - remove the body of this method as soon as you
   * can call a super method.
   */
  override def finish(docCount: Int) {
    dvType match {
      case FIXED_INTS_8  => byteWriter.write(getOrCreateDataOut())
      case FIXED_INTS_16 => shortWriter.write(getOrCreateDataOut())
      case FIXED_INTS_32 => intWriter.write(getOrCreateDataOut())
      case FIXED_INTS_64 => longWriter.write(getOrCreateDataOut())
      case FLOAT_32      => floatWriter.write(getOrCreateDataOut())
      case FLOAT_64      => doubleWriter.write(getOrCreateDataOut())
    }
    flushSlicesInfos()
  }

  /** Creates a output for a current writer */
  protected def getOrCreateDataOut(): IndexOutput = using(_dir.createOutput(currentWriteSliceFileName(_docValuesId), context)) {
      out =>
        CodecUtil.writeHeader(out, codecName, version)
        out
  }

  protected def getType: Type = dvType

  protected def docValuesId() = _docValuesId

  protected def dir()         = _dir
}
