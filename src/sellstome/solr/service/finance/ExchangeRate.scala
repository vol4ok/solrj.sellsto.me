package sellstome.solr.service.finance

import java.util.{Date, Currency}


/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 25.01.12
 * Time: 1:45
 * Represent currency exchange rate
 */
final class ExchangeRate(from: Currency, to: Currency, rate: Double) {

  private var date: Option[Date] = None

  def getFrom = from

  def getTo = to

  def getRate = rate

  def getDate = date

  /**The setter for the only optional field here */
  def setDate(date: Date): ExchangeRate = {
    if (date == null) this.date = None else this.date = Some(date)
    return this
  }

  /**@return a exchange rate for an inverse conversion */
  def inverse(): ExchangeRate = {
    val inverseRate = new ExchangeRate(to, from, 1 / rate)
    date.map(inverseRate.setDate(_))
    return inverseRate
  }

}