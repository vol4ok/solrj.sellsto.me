package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues
import org.apache.lucene.store.{IndexInput, IOContext, Directory}
import sellstome.lucene.codecs.DocValuesSlicesSupport
import org.apache.lucene.codecs.lucene40.values.Ints
import org.apache.lucene.util.{IOUtils, CodecUtil, Counter}
import org.apache.lucene.index.DocValues.{Source, Type}

/**
 * Reads stored INTS with the fixed bit precision
 * this class should be completely rewritten taking into account multiple slices
 * structure
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param _dir a directory is a flat list of files and operations on them
 * @param _docValuesId a unique identifier per segment && field
 */
class MutableDVReader(_dir: Directory,
                          _docValuesId: String,
                          bytesUsed: Counter,
                          maxDocs: Int,
                          context: IOContext,
                          dvType: Type) extends DocValues
                                        with DocValuesSlicesSupport {


  /** Holds a reference to data */
  protected var dataIn: List[IndexInput] = null

  protected val Source: PackedArraySourceFactory = PackedArraySource

  def getDirectSource(): Source = Source(dvType, cloneDataInputs())

  def load(): Source = Source(dvType, cloneDataInputs())

  def getType(): Type = dvType

  override def close() {
    try
      super.close()
    finally
      if (dataIn != null)
        dataIn.foreach(IOUtils.close( _ ))
  }

  protected def getDataInputs(): List[IndexInput] = {
    if (dataIn == null) {
      currentReadSlicesNames(_docValuesId).map[IndexInput, List[IndexInput]] { slice =>
        val input = _dir.openInput(slice, context)
        CodecUtil.checkHeader(input, Ints.CODEC_NAME, Ints.VERSION_CURRENT, Ints.VERSION_CURRENT)
        input
      }
    }
    dataIn
  }

  /** clones a given data input */
  protected def cloneDataInputs(): List[IndexInput]
      = getDataInputs().map(_.clone().asInstanceOf[IndexInput])

  protected def docValuesId(): String = _docValuesId

  protected def dir(): Directory = _dir

}