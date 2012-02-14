package sellstome.solr.response.json.converters

import org.apache.solr.schema.FieldType
import org.apache.lucene.index.IndexableField

/**
 * Defines how indexed field would be presented in json format.
 * @author Aliaksandr Zhuhrou
 */
abstract class SolrField2JsonConverter {

  /**converts to a json representation */
  def toJson(fieldType: FieldType, indexableField: IndexableField): Any

}