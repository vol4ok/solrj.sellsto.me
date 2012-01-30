package sellstome.search.solr.service.finance

import _root_.sellstome.search.solr.common.Disposable
import java.util.concurrent.Executors
import com.google.common.collect.Maps
import java.util.Currency
import collection.LinearSeq
import reactive.Observing

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


}

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 24.01.12
 * Time: 12:09
 * Provides currency exchange rates values from several sources. The problem with this service is that we do not
 * provide any defaults.
 */
class CurrencyExchangeRatesService extends ICurrencyExchangeRateService with Disposable with Observing {

  private val loaders = List(new TheEuropeanCentralBankLoader())

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

  private val ratesSnapshot = Maps.newConcurrentMap[ (Currency,Currency), ExchangeRate]()

  def getRate(from: Currency, to: Currency): Option[ExchangeRate] = {
    if (ratesSnapshot.containsKey( ( from , to ) )) {
      return Some(ratesSnapshot.get( ( from , to ) ))
    } else if (ratesSnapshot.containsKey( ( to , from ) )) {
      return Some( ratesSnapshot.get( ( to , from ) ).inverse() )
    }
    return None
  }

  /** Releases all resources */
  def close() {
    executor.shutdown()
  }

}