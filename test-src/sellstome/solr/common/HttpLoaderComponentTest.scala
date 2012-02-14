package sellstome.solr.common

import org.scalatest.FunSuite
import java.io.Reader

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 27.01.12
 * Time: 1:16
 * @tests {HttpLoader}
 */
class HttpLoaderComponentTest extends FunSuite with HttpLoader {

  test("Get Google Page") {
    val pageHtml = httpGet("https://www.google.com")
    assert(pageHtml != null)
  }
  
  test("Get Google Page As Stream") {
    httpGetStream[Unit]("https://www.google.com") {
      in => {
        assert(in.readLine() != null)
      }
    }
  }
  
  test("Get ECB Currency Rates XML") {
    val ratesXML = httpGet("http://www.ecb.int/stats/eurofxref/eurofxref-daily.xml")
    assert(ratesXML != null)
  }

}