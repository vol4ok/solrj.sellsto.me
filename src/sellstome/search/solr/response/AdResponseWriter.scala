package sellstome.search.solr.response

import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.{SolrQueryResponse, QueryResponseWriter}
import java.io.Writer
import org.apache.solr.search.DocList
import collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 11.10.11
 * Time: 0:50
 * Generates a json search response.
 */
class AdResponseWriter extends QueryResponseWriter {

  def write(writer: Writer, request: SolrQueryRequest, response: SolrQueryResponse) {
    val nl = response.getValues
  }

  def getContentType(request: SolrQueryRequest, response: SolrQueryResponse) = QueryResponseWriter.CONTENT_TYPE_XML_UTF8

  def init(args: NamedList[_]) {

  }

  /** Extract a search result list. */
  protected def extractSearchResults(nl: NamedList[_]): Option[DocList] = {
    var result: Option[DocList] = None
    nl.find( (entry) => entry.getValue match {
      case docs: DocList => {
        result = Some(docs)
        true
      }
      case _ => {
        false
      }
    })
    return result
  }

}