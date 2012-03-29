package sellstome.lucene.codecs.values

import org.apache.lucene.codecs.lucene40.values.PackedIntValues.PackedIntsWriter
import org.apache.lucene.util.Counter
import org.apache.lucene.store.{IOContext, Directory}
import sellstome.lucene.codecs.DocValuesSlicesSupport

/**
 * Stores integers using [[org.apache.lucene.util.packed.PackedInts]]
 * Aslo this class has a support for modification of the existing dv values without re-indexing the whole document.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutablePackedIntsDVConsumer(_dir: Directory,
                                  _docValuesId: String,
                                  bytesUsed: Counter,
                                  context: IOContext)
  extends PackedIntsWriter(_dir, _docValuesId, bytesUsed, context) with DocValuesSlicesSupport {

  def dir(): Directory = _dir

  def docValuesId(): String = _docValuesId

}
