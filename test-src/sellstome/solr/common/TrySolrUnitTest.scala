package sellstome.solr.common

import org.scalatest.FunSuite
import org.apache.solr.common.SolrException
import sellstome.BaseUnitTest

/**
 * Tests functionality of solr component exceptional wrapper
 * @author aliaksandr zhuhrou
 * @since 1.0
 */
class TrySolrUnitTest extends BaseUnitTest {

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
        Console.println("Succesfully catched expeption.")
      }
    }

  }

  test("test local return") {
      val result = trysolr {
        10
      }
      expect(10) (result)
  }
  
  test("test non local return") {
     val result = calculate()
     expect(1000) (result)
  }
  
  def calculate(): Int = {
    trysolr {
      return 1000
    }
    fail()
  }

}