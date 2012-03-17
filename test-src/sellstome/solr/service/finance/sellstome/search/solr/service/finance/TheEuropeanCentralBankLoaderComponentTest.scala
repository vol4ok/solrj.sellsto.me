package sellstome.solr.service.finance.sellstome.search.solr.service.finance

import sellstome.solr.service.finance.TheEuropeanCentralBankLoader
import sellstome.BaseUnitTest

/**
 * Tests the [[sellstome.solr.service.finance.TheEuropeanCentralBankLoader]]
 * User: Alexander Zhugrov
 */
class TheEuropeanCentralBankLoaderComponentTest extends BaseUnitTest {

  test("Test poll() method") {
    val loader = new TheEuropeanCentralBankLoader()
    val list   = loader.poll()
    assert(list != null)
  }

}