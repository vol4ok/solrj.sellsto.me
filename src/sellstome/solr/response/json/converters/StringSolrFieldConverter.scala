package sellstome.solr.response.json.converters

import org.apache.solr.schema.FieldType
import org.apache.lucene.index.IndexableField

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/6/11
 * Time: 10:38 PM
 * Converts stored string to a json string
 *  location":"40.87888996153335,-74.24399738342458"
 */
object StringSolrFieldConverter extends SolrField2JsonConverter {
  def toJson(fieldType: FieldType, indexableField: IndexableField) = indexableField.stringValue()
}