package sellstome.solr.response.json

import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import java.io.Writer
import org.apache.solr.response.{SolrQueryResponse, QueryResponseWriter}


object JSONResponseWriter {

  /**content type */
  val CONTENT_TYPE_JSON_UTF8: String = "text/x-json; charset=UTF-8"

}

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 11.10.11
 * Time: 0:50
 * Generates a json search response.
 */
class JSONResponseWriter extends QueryResponseWriter {

  /**
   * Write a SolrQueryResponse, this method must be thread save.
   */
  def write(writer: Writer, request: SolrQueryRequest, response: SolrQueryResponse) {
    JSONSerializer.writeResponse(writer, request, response)
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

}