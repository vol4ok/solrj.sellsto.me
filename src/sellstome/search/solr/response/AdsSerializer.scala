package sellstome.search.solr.response

import org.apache.solr.search.DocList
import org.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 11.10.11
 * Time: 1:36
 * Based on the predefined knowledge of the ad solr schema it
 * does serialization to json for the ad search request.
 */
object AdsSerializer {

  /** Transform to a json array */
  def apply(docs: DocList): JSONObject = {
    val json = new JSONObject()
    while (docs.iterator().hasNext) {
      val doc = docs.iterator().next()
    }
    return json
  }

}