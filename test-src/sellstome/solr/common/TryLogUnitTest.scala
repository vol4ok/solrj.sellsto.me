package sellstome.search.solr.common

import org.scalatest.FunSuite
import org.apache.solr.common.SolrException
import org.slf4j.{Logger, LoggerFactory}

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 02.02.12
 * Time: 1:44
 * Test trylog control structure
 */
class TryLogUnitTest extends FunSuite {

  implicit val log: Logger = LoggerFactory.getLogger(classOf[TryLogUnitTest])
  
  test("Test ok case") {
    trylog {
      Console.println("Nothing interesting happens")
    }
  }
  
  test("Test solr exception") {
    try {
      trylog {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Created for test purposes")
      }
      fail("Make sure that control structure rethrow the exception")
    }
    catch {
      case e: SolrException => {
        Console.println("Catch exception")
        assert( e.getCause == null )
      }
    }
  }
  
  test("Test runtime exception") {
    try {
      trylog {
        throw new RuntimeException("Created for test purposes")
      }
      fail("Make sure that control structure rethrow the exception")
    }
    catch {
      case e: RuntimeException => Console.println("Catch exception")
    }
  }

}
