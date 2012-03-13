package sellstome.lucene.codecs

import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.index.FieldInfo
import org.apache.lucene.codecs.{DocValuesConsumer, PerDocConsumer}

/**
 *
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesConsumer extends PerDocConsumer {

  def addValuesField(`type`: Type, field: FieldInfo): DocValuesConsumer = throw new NotImplementedError()

  def abort() {throw new NotImplementedError()}

  def close() {throw new NotImplementedError()}

}
