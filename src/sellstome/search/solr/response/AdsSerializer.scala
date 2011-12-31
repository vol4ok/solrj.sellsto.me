package sellstome.search.solr.response

import org.apache.solr.search.{SolrIndexSearcher, DocList}
import org.apache.lucene.document.Document
import javax.annotation.Nonnull
import org.apache.solr.common.params.{CommonParams, SolrParams}
import org.json.{JSONObject, JSONArray}

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 11.10.11
 * Time: 1:36
 * Based on the predefined knowledge of the ad solr schema it
 * does serialization to json for the ad search request.
 */
object AdsSerializer {

  /** Transform to a json array
   *  @param docs An ordered list of document ids
   *  @param searcher Implements search functionality over single index reader
   */
  def apply(@Nonnull docs: DocList,@Nonnull searcher: SolrIndexSearcher): JSONArray = {
    val jsonDocs = new JSONArray()
    val docIterator = docs.iterator()
    for (i <- 1 to docs.size()) {
      val docId = docIterator.next()
      jsonDocs.put(adDocToJson(searcher.doc(docId)))
    }
    return jsonDocs
  }

  /**
   *  Transforms a single entry to a json string
   *  @param doc A search result unit
   *  @return
   */
  private def adDocToJson(doc: Document): JSONObject = {
    val adJson = new JSONObject()
    AdSchema.values.foreach[Unit]( (field) =>
      adJson.put( field.getResponseFieldName , field.toJson(doc.get(field.getFieldName)))
    )
    //todo add fake fields for now
    adJson.put("updated_at", "2011-07-07T16:37:33.000Z")
    adJson.put("author", "vol4ok")
    adJson.put("avator", new JSONObject().put("name","av-1").put("type","png"))
    adJson.put("count", 12)
    adJson.put("created_at", "2011-07-07T16:37:33.000Z")
    adJson.put("images", new JSONArray()
      .put(new JSONObject()
           .put("name","item-1")
           .put("type","png")))
    return adJson
  }


}