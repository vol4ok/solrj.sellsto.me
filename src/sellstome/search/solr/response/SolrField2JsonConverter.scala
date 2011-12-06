package sellstome.search.solr.response

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/6/11
 * Time: 10:33 PM
 * Define how indexed field would be presented in json format.
 */
abstract class SolrField2JsonConverter {

  /** converts to a json representation */
  def toJson(storedField: String): Any;

}