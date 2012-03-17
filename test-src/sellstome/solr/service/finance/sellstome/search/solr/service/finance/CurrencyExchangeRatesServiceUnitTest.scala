package sellstome.solr.service.finance.sellstome.search.solr.service.finance

import org.scalatest.FunSuite
import sellstome.solr.util.Currencies
import sellstome.solr.service.finance.{ExchangeRate, CurrencyExchangeRatesService}
import sellstome.solr.schema.finance.MoneyValue
import sellstome.BaseUnitTest

/**
 * Tests methods in [[sellstome.solr.service.finance.CurrencyExchangeRatesService]]
 * @author Alexander Zhugrov
 * @since 1.0
 */
class CurrencyExchangeRatesServiceUnitTest extends BaseUnitTest {
  
  test("CurrencyExchangeRatesService#convertCurrency") {
    val service = new CurrencyExchangeRatesService() {
      ratesSnapshot.put((Currencies("USD"), Currencies("EUR")), new ExchangeRate(Currencies("USD"), Currencies("EUR"), 0.5d))
    }

    val source = new MoneyValue(200, Currencies("USD"))
    val converted = service.convertCurrency(source, Currencies("EUR"))
    expect ( Currencies("EUR") ) ( converted.getCurrency )
    expect ( 100 ) ( converted.getAmount )
  }

}
