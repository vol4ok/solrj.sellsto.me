package sellstome.solr.common

import sellstome.BaseUnitTest

/**
 * Tests a [[sellstome.solr.common.HttpLoader]]
 * @author Alexander Zhugrov
 * @since 1.0
 */
class HttpLoaderComponentTest extends BaseUnitTest with HttpLoader {

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