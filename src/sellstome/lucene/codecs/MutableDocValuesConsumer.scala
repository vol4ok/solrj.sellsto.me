package sellstome.lucene.codecs

import org.apache.lucene.codecs.DocValuesConsumer
import org.apache.lucene.index.IndexableField
import org.apache.lucene.store.{IOContext, Directory}
import org.apache.lucene.index.DocValues.Type

/**
 * API for per-document stored primitive values of type
 * <tt>long</tt> or <tt>double</tt>. The API accepts a single value for each
 * document. In future we plan support for modification for a given segment.
 * <p>
 * todo zhugrov a - Make document Id passing to this class in out of order.
 * </p>
 * note: that currently we support only subset of all [[org.apache.lucene.index.DocValues.Type]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesConsumer(segment: String, dir: Directory, ctx: IOContext, docValuesType: Type, segmentSuffix: String) extends DocValuesConsumer {

  protected def getType() = docValuesType

  /**
   * Adds the given [[org.apache.lucene.index.IndexableField]] instance to this
   * [[org.apache.lucene.codecs.DocValuesConsumer]]
   *
   * @param docID the document ID to add the value for.
   * @param value the value to add
   * @throws IOException
   */
  def add(docID: Int, value: IndexableField) {

    throw new NotImplementedError()
  }

  /**
   * Called when the consumer of this API is done adding values.
   *
   * @param docCount  the total number of documents in this [[org.apache.lucene.codecs.DocValuesConsumer]].
   * Must be greater than or equal the last given docID to [[sellstome.lucene.codecs.MutableDocValuesConsumer#add(int, IndexableField)]].
   * @throws IOException
   */
  def finish(docCount: Int) {

    throw new NotImplementedError()
  }

}
