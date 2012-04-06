package sellstome.lucene.codecs

import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.util.Counter
import org.apache.lucene.store.{IOContext, Directory}
import org.apache.lucene.codecs.DocValuesConsumer

/**
 * @author Aliaksandr Zhuhrou
 */
trait DocValuesConsumerFactory {
  def create(dvType: Type, docValuesId: String, dir: Directory, bytesUsed: Counter, context: IOContext): DocValuesConsumer
}