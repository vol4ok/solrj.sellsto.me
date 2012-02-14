package sellstome.solr.response.json.converters

import org.json.JSONObject
import org.apache.solr.schema.FieldType
import org.apache.lucene.index.IndexableField

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/6/11
 * Time: 11:04 PM
 * Converts solr location string to a json object.
 */
object LocationSolrFieldConverter extends SolrField2JsonConverter {
  def toJson(fieldType: FieldType, indexableField: IndexableField) = {
    val latLongPair = indexableField.stringValue().split(",")
    new JSONObject()
      .put("lat", latLongPair(0).toDouble)
      .put("lng", latLongPair(1).toDouble)
  }
}