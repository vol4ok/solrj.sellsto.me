package sellstome.lucene.codecs

import org.apache.lucene.codecs.PerDocConsumer
import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.index.FieldInfo

/**
 *
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesConsumer extends PerDocConsumer {

  def addValuesField(`type`: Type, field: FieldInfo) = throw new NotImplementedError()

  def abort() {throw new NotImplementedError()}

  def close() {throw new NotImplementedError()}

}
