package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues.{Source, Type}
import org.apache.lucene.index.DocValues.Type._
import sellstome.lucene.codecs.DocValuesSlicesSupport
import org.apache.lucene.store.{IndexOutput, IOContext, Directory}
import org.apache.lucene.codecs.lucene40.values.Ints
import org.apache.lucene.codecs.DocValuesConsumer
import org.apache.lucene.index.{DocValues, IndexableField}
import org.apache.lucene.util.{Bits, IOUtils, CodecUtil, Counter}
import sellstome.lucene.io.packed.array._
import org.apache.lucene.document.Field

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
class MutableDVConsumer(    _dir: Directory,
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
  protected var dataOut: IndexOutput = null


  //region Indexing Support
  override def add(docID: Int, value: IndexableField) {
    dvType match {
      case FIXED_INTS_8  => byteWriter.add(docID,   value.numericValue().byteValue())
      case FIXED_INTS_16 => shortWriter.add(docID,  value.numericValue().shortValue())
      case FIXED_INTS_32 => intWriter.add(docID,    value.numericValue().intValue())
      case FIXED_INTS_64 => longWriter.add(docID,   value.numericValue().longValue())
      case FLOAT_32      => floatWriter.add(docID,  value.numericValue().floatValue())
      case FLOAT_64      => doubleWriter.add(docID, value.numericValue().doubleValue())
      case _             => throw new IllegalStateException(s"Not supported dvType: ${dvType}")
    }
  }

  /**
   * write a processed values to a disk.
   */
  override def finish(docCount: Int) {
    var hasException = false
    try {
      dvType match {
        case FIXED_INTS_8   => byteWriter.write(getOrCreateDataOut())
        case FIXED_INTS_16  => shortWriter.write(getOrCreateDataOut())
        case FIXED_INTS_32  => intWriter.write(getOrCreateDataOut())
        case FIXED_INTS_64  => longWriter.write(getOrCreateDataOut())
        case FLOAT_32       => floatWriter.write(getOrCreateDataOut())
        case FLOAT_64       => doubleWriter.write(getOrCreateDataOut())
        case _              => throw new IllegalStateException(s"Not supported dvType: ${dvType}")
      }
    } catch {
      case e: Throwable => hasException = true
    } finally {
      if (hasException) {
        IOUtils.closeWhileHandlingException(dataOut)
      } else {
        IOUtils.close(dataOut)
      }
    }
    flushSlicesInfos()
  }
  //endregion


  //region Merge Support
  protected override def merge(reader: DocValues, docBase: Int, docCount: Int, liveDocs: Bits) {
    val source = reader.getDirectSource()
    (0 until docCount).foldLeft(docBase) {(mergedDocId, oldDocId) =>
      if (liveDocs == null || liveDocs.get(oldDocId)) {
        dvType match {
          case FIXED_INTS_8   => byteWriter.add(mergedDocId, source.getInt(oldDocId).toByte)
          case FIXED_INTS_16  => shortWriter.add(mergedDocId, source.getInt(oldDocId).toShort)
          case FIXED_INTS_32  => intWriter.add(mergedDocId, source.getInt(oldDocId).toInt)
          case FIXED_INTS_64  => longWriter.add(mergedDocId, source.getInt(oldDocId))
          case FLOAT_32       => floatWriter.add(mergedDocId, source.getFloat(oldDocId).toFloat)
          case FLOAT_64       => doubleWriter.add(mergedDocId, source.getFloat(oldDocId))
        }
        mergedDocId+1
      } else {
        mergedDocId
      }
    }
  }



  protected override def mergeDoc(scratchField: Field, source: Source, docID: Int, sourceDoc: Int) {
    //do nothing here. Disabling the parent functionality
  }
  //endregion

  //region Utility Methods
  /** Creates a output for a current writer */
  protected def getOrCreateDataOut(): IndexOutput = {
    if (dataOut == null) {
      dataOut =_dir.createOutput(currentWriteSliceName(_docValuesId), context)
      CodecUtil.writeHeader(dataOut, codecName, version)
    }
    return dataOut
  }

  protected def getType: Type = dvType

  protected def docValuesId() = _docValuesId

  protected def dir()         = _dir
  //endregion
}
