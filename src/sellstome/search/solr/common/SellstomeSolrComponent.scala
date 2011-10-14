package sellstome.search.solr.common

import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrException.ErrorCode

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 14.10.11
 * Time: 0:15
 * Contains common utility functions.
 */
trait SellstomeSolrComponent {

  /** Check that condition is true and throw a solr exception otherwise */
  protected def ensuring(cond: Boolean, message: String) {
    if (!cond) throw new SolrException(ErrorCode.SERVER_ERROR, message)
  }


}