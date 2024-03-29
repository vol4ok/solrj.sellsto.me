package sellstome.solr.common

import org.apache.solr.common.SolrException
import runtime.NonLocalReturnControl

/**
 * In many solr components we are using a following pattern for wrapping exceptions
 *  try {
 *     return new MoneyValue(java.lang.Long.parseLong(amount), code)
 *  }
 *  catch {
 *     case e: NumberFormatException => {
 *       throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e)
 *     }
 *  }
 *
 *  Instead of that we may introduce a wrapper that is capable of handling this by default
 *  trysolr {
 *    some stuff to do
 *  }
 *
 *  @author Aliaksandr Zhuhrou
 */
object trysolr {

  def apply[T](f: => T): T = {
    try {
      f
    } catch {
      case e: NonLocalReturnControl[_] => throw (e)
      case e: SolrException => throw (e)
      case e: Throwable => throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e)
    }
  }

}