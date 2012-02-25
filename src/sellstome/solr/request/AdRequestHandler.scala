package sellstome.solr.request

import org.apache.solr.handler.component.SearchHandler
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse

/**
 * Parses a select ad query.
 * Main goal of this class is to transform a custom parameters that defines a geo bounding box to a solr Filter Query
 * @author aliaksandr zhuhrou
 */
class AdRequestHandler extends SearchHandler {

  override def handleRequestBody(req: SolrQueryRequest, rsp: SolrQueryResponse) {
    RequestParams.LocationBound.populateFQQuery(req)
    super.handleRequestBody(req, rsp)
  }

}