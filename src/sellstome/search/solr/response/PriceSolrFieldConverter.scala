package sellstome.search.solr.response

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/7/11
 * Time: 12:17 AM
 * Converts a price solr field.
 */
object PriceSolrFieldConverter extends SolrField2JsonConverter {

  def toJson(storedField: String) = storedField.toDouble.toInt

}