package sellstome.lucene.codecs

import org.apache.lucene.codecs.PerDocProducer

/**
 *
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesProducer extends PerDocProducer {

  def docValues(field: String) = throw new NotImplementedError()

  def close() {throw new NotImplementedError()}

}
