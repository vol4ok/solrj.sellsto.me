package sellstome.search.solr.common

import org.scalatest.FunSuite
import org.apache.solr.common.SolrException

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 25.01.12
 * Time: 2:35
 * Tests functionality of solr component exceptional wrapper
 */
class SolrExceptionalWrapperClauseTest extends FunSuite {

  test("test normal case") {

    trysolr {
      Console.println("some stuff to do")
    }

  }

  test("error case") {
    try {
      trysolr {
        throw new RuntimeException("just for fun")
      }
      fail()
    } catch {
      case e: SolrException => {

      }
    }

  }

}