package sellstome.search.solr.service.finance

import _root_.sellstome.search.solr.common.Disposable
import _root_.sellstome.search.solr.schema.finance.MoneyValue
import java.util.concurrent.Executors
import com.google.common.collect.Maps
import java.util.Currency
import reactive.Observing
import org.slf4j.{LoggerFactory, Logger}
import org.apache.solr.common.SolrException
import javax.annotation.Nonnull
import java.math.{BigInteger, RoundingMode}

/** Public service interface */
trait ICurrencyExchangeRateService {

  /**
   * Returns the currently known exchange rate between two currencies. If a direct rate has been loaded,
   * it is used. Otherwise, if a rate is known to convert the target currency to the source, the inverse
   * exchange rate is computed.
   *
   * @param from The source currency being converted from.
   * @param to   The target currency being converted to.
   * @return The exchange rate.
   */
  def getRate(from: Currency, to: Currency): Option[ExchangeRate];

  /**
   * Converts a value from one currency to another.
   * @param value a source value
   * @param to a target currency
   * @throws SolrException in case if could not find a corresponding exchange rate.
   */
  def convertCurrency(@Nonnull value: MoneyValue, @Nonnull to: Currency): MoneyValue;

}

object CurrencyExchangeRatesService {
  /** logger instance */
  val log: Logger = LoggerFactory.getLogger(classOf[CurrencyExchangeRatesService])
  
}

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 24.01.12
 * Time: 12:09
 * Provides currency exchange rates values from several sources. The problem with this service is that we do not
 * provide any defaults.
 */
class CurrencyExchangeRatesService extends ICurrencyExchangeRateService
                                   with Disposable
                                   with Observing {

  protected val loaders = List(new TheEuropeanCentralBankLoader())

  /** do an actual work on pulling data about exchange rates */
  private val executor = Executors.newSingleThreadScheduledExecutor()
  loaders.foreach( (loader) => {
    executor.scheduleAtFixedRate(loader, 0l, loader.frequency()._1, loader.frequency()._2 )
    on(loader.change) {
      //NOTE zhugrov a - we invoke this function in a background thread
      rates =>
        Console.println("Rates loaded event received. " + rates)
        rates.foreach( _.foreach( (rate) => ratesSnapshot.put( (rate.getFrom,rate.getTo), rate ) ))
    }
  })

  protected val ratesSnapshot = Maps.newConcurrentMap[ (Currency,Currency), ExchangeRate]()

  def getRate(from: Currency, to: Currency): Option[ExchangeRate] = {
    if (ratesSnapshot.containsKey( ( from , to ) )) {
      return Some(ratesSnapshot.get( ( from , to ) ))
    } else if (ratesSnapshot.containsKey( ( to , from ) )) {
      return Some( ratesSnapshot.get( ( to , from ) ).inverse() )
    } else if (from == to) {
      return Some(new ExchangeRate(from, to, 1.0d))
    }
    return None
  }

  def convertCurrency(value: MoneyValue, to: Currency): MoneyValue = {
    val exchangeRate = getRate( value.getCurrency, to )
    if (exchangeRate.isEmpty) throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
      "Could not find exchange rates for a given currencies: "+value.getCurrency+","+to)
    val sourceAmount = new java.math.BigDecimal(java.math.BigInteger.valueOf(value.getAmount), value.getCurrency.getDefaultFractionDigits)
    val rateValue = new java.math.BigDecimal(exchangeRate.get.getRate)
    val targetAmount = (sourceAmount multiply rateValue).setScale(to.getDefaultFractionDigits, RoundingMode.HALF_UP)
    return new MoneyValue(targetAmount.unscaledValue().longValue(), to)
  }

  /** Releases all resources */
  def close() {
    executor.shutdown()
  }

}