package sellstome.search.solr.response

import org.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/6/11
 * Time: 11:04 PM
 * Converts solr location string to a json object.
 */
object LocationSolrFieldConverter extends SolrField2JsonConverter {
  def toJson(storedField: String) = {
    val latLongPair = storedField.split(",")
    new JSONObject()
      .put("latitude",  latLongPair(0).toDouble)
      .put("longitude", latLongPair(1).toDouble)
  }
}