package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues
import sellstome.lucene.codecs.DocValuesSlicesSupport

/**
 * Reads data stored as integers using [[org.apache.lucene.util.packed.PackedInts]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutablePackedIntsDVReader extends DocValues
                                with DocValuesSlicesSupport {
  def load() = ???

  def getDirectSource = ???

  def getType = ???

  protected def docValuesId() = ???

  protected def dir() = ???

}
