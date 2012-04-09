package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues
import org.apache.lucene.codecs.DocValuesArraySource
import org.apache.lucene.store.{IndexInput, IOContext, Directory}
import sellstome.control.using
import sellstome.lucene.codecs.DocValuesSlicesSupport
import org.apache.lucene.codecs.lucene40.values.Ints
import org.apache.lucene.util.{IOUtils, CodecUtil, Counter}
import org.apache.lucene.index.DocValues.{Source, Type}
import org.apache.lucene.codecs.lucene40.values.FixedStraightBytesImpl.DirectFixedStraightSource

/**
 * Reads stored INTS with the fixed bit precision
 * this class should be completely rewritten taking into account multiple slices
 * structure
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param _dir a directory is a flat list of files and operations on them
 * @param _docValuesId a unique identifier per segment && field
 */
class MutableIntsDVReader(_dir: Directory,
                          _docValuesId: String,
                          bytesUsed: Counter,
                          maxDocs: Int,
                          context: IOContext,
                          dvType: Type) extends DocValues
                                        with DocValuesSlicesSupport {

  /** todo zhugrov a - clarify its purpose. */
  protected val arrayTemplate: DocValuesArraySource = DocValuesArraySource.forType(dvType)
  /** A fixed bit size for a current integer data type */
  protected val size: Int = getDataInput().readInt()
  /** Holds a reference to data */
  protected var dataIn: IndexInput = null

  def getDirectSource(): Source = new DirectFixedStraightSource(cloneDataIn(), size, dvType)

  def load(): Source = {
    using(cloneDataIn()) { input => arrayTemplate.newFromArray(input) }
  }

  def getType(): Type = dvType

  override def close() {
    try
      super.close()
    finally
      IOUtils.close(dataIn)
  }

  protected def getDataInput(): IndexInput = {
    if (dataIn == null) {
      dataIn = using(_dir.openInput(currentWriteSliceFileName(_docValuesId), context)) {
        in =>
          CodecUtil.checkHeader(in, Ints.CODEC_NAME, Ints.VERSION_CURRENT, Ints.VERSION_CURRENT)
          in
      }
    }
    return dataIn
  }

  /** clones a given data input */
  protected def cloneDataIn(): IndexInput
      = getDataInput().clone().asInstanceOf[IndexInput]

  protected def docValuesId(): String = _docValuesId

  protected def dir(): Directory = _dir

}