package sellstome.lucene.codecs.values

import org.apache.lucene.codecs.lucene40.values.Ints
import org.apache.lucene.util.Counter
import org.apache.lucene.store.{IOContext, Directory}
import org.apache.lucene.index.DocValues.Type
import sellstome.lucene.codecs.DocValuesSlicesSupport

/**
 * Stores ints packed and fixed with fixed-bit precision.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 *
 */
class IntsMutableDVConsumer(dir: Directory, dvFieldId: String, codecName: String,
                            version: Int, bytesUsed: Counter, context: IOContext, valueType: Type)
  extends Ints.IntsWriter(dir, dvFieldId, codecName, version, bytesUsed, context, valueType)
  with DocValuesSlicesSupport {

  def this(dir: Directory, id: String, bytesUsed: Counter, context: IOContext, valueType: Type) =
    this(dir, id, Ints.CODEC_NAME, Ints.VERSION_CURRENT, bytesUsed, context, valueType)

}
