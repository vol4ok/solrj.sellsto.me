package sellstome.lucene.codecs

import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.util.Counter
import org.apache.lucene.store.{IOContext, Directory}
import org.apache.lucene.codecs.DocValuesConsumer
import values.MutableDVConsumer

/**
 * @author Aliaksandr Zhuhrou
 */
object MutableDocValuesConsumerFactory extends DocValuesConsumerFactory {
  /**
   * Factory method to create a [[org.apache.lucene.codecs.DocValuesConsumer]] instance for a given type. This
   * method returns default implementations for each of the different types
   * defined in the [[org.apache.lucene.index.DocValues.Type]]enumeration.
   *
   * @param docValuesId the file name id used to create files within the writer.
   * @param dir the [[org.apache.lucene.store.Directory]] to create the files from.
   * @param bytesUsed a byte-usage tracking reference
   * @return a new [[org.apache.lucene.codecs.DocValuesConsumer]] instance for the given [[org.apache.lucene.index.DocValues.Type]]
   * @throws IOException if could not create a consumer
   */
  def create(dvType: Type, docValuesId: String, dir: Directory,
             bytesUsed: Counter, context: IOContext): DocValuesConsumer = {
    import Type._
    return dvType match {
      case FIXED_INTS_16  => new MutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case FIXED_INTS_32  => new MutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case FIXED_INTS_64  => new MutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case FIXED_INTS_8   => new MutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case _              => throw new IllegalArgumentException("Not supported doc values type: " + dvType)
    }
  }
}