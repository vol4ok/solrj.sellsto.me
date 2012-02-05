package sellstome.search.solr.common

import org.apache.solr.common.SolrException
import org.slf4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: Alex Zh
 * Date: 25.01.12
 * Time: 8:58
 * Wrapper for cases when the only thing we do is
 *
 *   try {
 *     .. do some stuff
 *   catch {
 *     case e : ParserConfigurationException => {
 *     SolrException.log(log, "Exception during parsing file: " + name, e)
 *     throw e
 *   }
 *   case e : SAXException => {
 *     SolrException.log(log, "Exception during parsing file: " + name, e)
 *     throw e
 *   }
 *   case e : SolrException => {
 *     SolrException.log(log, "Error in " + name, e)
 *     throw e
 *   }}
 *
 * to any exception that may occur inside of block of code.
 */
object trylog {

  def apply(f: => Unit) (implicit log: Logger) {
    try {
      f
    } catch {
      case e: Throwable => {
        SolrException.log(log, e)
        throw e
      }
    }
  }

}