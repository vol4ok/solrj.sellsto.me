package sellstome.search.solr.request

import org.apache.solr.handler.StandardRequestHandler
import org.apache.solr.handler.component.SearchHandler
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.common.params.DefaultSolrParams

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/8/11
 * Time: 11:47 PM
 * Parses a select ad query.
 * Main goal of this class is to transform a custom parameters that defines a geo bounding box to a solr Filter Query
 */
class AdRequestHandler extends SearchHandler {

  override def handleRequestBody(req: SolrQueryRequest, rsp: SolrQueryResponse) {
    RequestParams.LocationBound.populateFQQuery(req)
    super.handleRequestBody(req,rsp)
  }

}