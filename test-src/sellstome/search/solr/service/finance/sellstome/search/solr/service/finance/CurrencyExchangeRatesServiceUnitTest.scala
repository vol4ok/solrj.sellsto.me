package sellstome.search.solr.service.finance.sellstome.search.solr.service.finance

import org.scalatest.FunSuite
import java.util.Currency
import sellstome.search.solr.util.Currencies
import sellstome.search.solr.service.finance.{ExchangeRate, CurrencyExchangeRatesService}
import sellstome.search.solr.schema.finance.MoneyValue

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 03.02.12
 * Time: 9:08
 * Tests methods in {@link CurrencyExchangeRatesService}
 */
class CurrencyExchangeRatesServiceUnitTest extends FunSuite {
  
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
