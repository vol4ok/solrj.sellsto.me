package sellstome.search.solr.response

import org.apache.solr.search.{SolrIndexSearcher, DocList}
import org.apache.lucene.document.Document
import org.json.{JSONObject, JSONArray}
import javax.annotation.Nonnull
import org.apache.solr.common.params.{CommonParams, SolrParams}

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
  def apply(@Nonnull docs: DocList,@Nonnull searcher: SolrIndexSearcher): JSONObject = {
    val json = new JSONObject()
    val jsonDocs = new JSONArray()
    for (i <- 1 to docs.size()) {
      val docId = docs.iterator().next()
      jsonDocs.put(adDocToJson(searcher.doc(docId)))
    }
    json.put("results", jsonDocs)
    json.put( CommonParams.ROWS, docs.size())
    json.put( CommonParams.START, docs.offset())
    return json
  }

  /**
   *  Transforms a single entry to a json string
   *  @param doc A search result unit
   *  @return
   */
  private def adDocToJson(doc: Document): JSONObject = {
    val adJson = new JSONObject()
    AdSchema.values.foreach[Unit]( (field) =>
      adJson.put( field.getFieldName , doc.get(field.getFieldName) )
    )
    return adJson
  }


}