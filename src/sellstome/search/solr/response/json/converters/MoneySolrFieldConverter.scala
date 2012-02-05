package sellstome.search.solr.response.json.converters

import org.apache.solr.schema.FieldType
import org.apache.lucene.index.IndexableField
import sellstome.search.solr.common.NotImplementedException

/**
 *
 * @author Aliaksandr Zhuhrou
 */
object MoneySolrFieldConverter extends SolrField2JsonConverter {
  def toJson(fieldType: FieldType, indexableField: IndexableField) =
    //todo zhugrov a - provide the implementation of this method
    indexableField.stringValue()
}
