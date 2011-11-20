package sellstome.search.solr.response

import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import java.io.Writer
import org.apache.solr.search.DocList
import collection.JavaConversions._
import javax.annotation.Nonnull
import org.apache.solr.common.SolrException
import sellstome.search.solr.common.SellstomeSolrComponent
import org.apache.solr.response.{ResultContext, SolrQueryResponse, QueryResponseWriter}

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 11.10.11
 * Time: 0:50
 * Generates a json search response.
 */
class AdResponseWriter extends QueryResponseWriter with SellstomeSolrComponent {
  /** content type */
  val CONTENT_TYPE_JSON_UTF8: String = "text/x-json; charset=UTF-8"

  /**
   * Write a SolrQueryResponse, this method must be thread save.
   */
  def write(writer: Writer, request: SolrQueryRequest, response: SolrQueryResponse) {
    writer.write(AdsSerializer(extractSearchResults(response.getValues),
                 request.getSearcher).toString)
  }

  /**
   * Return the applicable Content Type for a request, this method
   * must be thread safe.
   */
  def getContentType(request: SolrQueryRequest, response: SolrQueryResponse) = QueryResponseWriter.CONTENT_TYPE_TEXT_UTF8

  /**<code>init</code> will be called just once, immediately after creation.
   * <p>The args are user-level initialization parameters that
   * may be specified when declaring a response writer in
   * solrconfig.xml
   */
  def init(args: NamedList[_]) {
    //do nothing right now
  }

  /** Extract a search result list.
   *  @param responseData data to be returned in solr response
   *  @throws org.apache.solr.common.SolrException could not parse responseData
   */
  @Nonnull
  protected def extractSearchResults(responseData: NamedList[_]): DocList = {
    var result: DocList = null
    responseData.find( (entry) => entry.getValue match {
      case resultContext: ResultContext => {
        result = resultContext.docs
        true
      }
      case _ => {
        false
      }
    })
    ensuring( result != null , "Could not find a DocList collection")
    return result
  }

}