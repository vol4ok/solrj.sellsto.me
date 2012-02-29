package sellstome.solr.schema

import org.apache.solr.schema.{SchemaField, FieldType}
import org.apache.solr.response.TextResponseWriter
import org.apache.lucene.search.SortField
import org.apache.lucene.index.{DocValues, IndexableField}
import org.apache.lucene.document.DocValuesField


/**
 * Represent values that similar to Google's PageRank.
 * It allows us include in calculating of ranking the
 * static signals such us author popularity, number of
 * document view and etc.
 * todo zhugrov a - much of its implementation is currently undefined.
 * @author Aliaksandr Zhuhrou
 */
class DocRankFieldType extends FieldType {

  /**
   * Used for adding a document when a field needs to be created from a
   * type and a string.
   * @param fieldType should be ignored here. Added for compatibility with a solr api
   */
  override protected def createField(name: String, value: String, fieldType: org.apache.lucene.document.FieldType, boost: Float): IndexableField = {
    return new DocValuesField(name, value.toLong, DocValues.Type.VAR_INTS)
  }

  def write(writer: TextResponseWriter, name: String, f: IndexableField) {}

  /**
   *
   * @throws UnsupportedOperationException we do not support a sort operation for this type
   */
  def getSortField(field: SchemaField, top: Boolean): SortField = {
    throw new UnsupportedOperationException("We do not support a sort operation for values of this type")
  }

}