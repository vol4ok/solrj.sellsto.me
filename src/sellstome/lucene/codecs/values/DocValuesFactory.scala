package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues
import org.apache.lucene.store.{IOContext, Directory}

/**
 * A factory for creating the doc values that
 * a dense per-document typed storage for fast access based
 * on lucene internal doc id.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DocValuesFactory {

  /**
   * Creates a new dv reader for doc values for a given field and segment
   * @param docCount ???
   * @param dir a flat access to a list of files
   * @param dvId an id of given doc values file. segmentName + fieldName combination.
   * @param dvType a type for dv field
   * @param context IO context
   * @return reader for a given dv field in a given segment
   */
   def docValues(docCount: Int,
                 dir: Directory,
                 dvId: String,
                 dvType: DocValues.Type,
                 context: IOContext): DocValues

}