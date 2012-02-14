package sellstome.solr.service.finance.sellstome.search.solr.service.finance

import org.scalatest.FunSuite
import sellstome.solr.service.finance.TheEuropeanCentralBankLoader

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 27.01.12
 * Time: 4:10
 * @tests {TheEuropeanCentralBankLoader}
 */
class TheEuropeanCentralBankLoaderComponentTest extends FunSuite {

  test("Test poll() method") {
    val loader = new TheEuropeanCentralBankLoader()
    val list   = loader.poll()
    assert(list != null)
  }

}