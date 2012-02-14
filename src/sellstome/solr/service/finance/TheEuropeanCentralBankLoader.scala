package sellstome.solr.service.finance

import _root_.sellstome.solr.common.{HttpLoader, XmlParser}
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.io.BufferedReader
import java.util.Currency
import java.text.SimpleDateFormat
import collection.LinearSeq
import reactive.{EventStream, Signal}

object TheEuropeanCentralBankLoader {

  val Log = LoggerFactory.getLogger(classOf[TheEuropeanCentralBankLoader])

}

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 25.01.12
 * Time: 8:27
 * Uses a data provided by The European Central Bank
 */
class TheEuropeanCentralBankLoader extends RateLoader
                                   with XmlParser
                                   with HttpLoader {

  private val DataSourceUrl = "http://www.ecb.int/stats/eurofxref/eurofxref-daily.xml"

  private val BaseCurrency = Currency.getInstance("EUR")

  private val DateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def poll(): LinearSeq[ExchangeRate] = {
    prepare()
    return matchXmlList("/Envelope/Cube/Cube/Cube", rateAttributes()).map( (attributes) => {
      val rate = new ExchangeRate( BaseCurrency, Currency.getInstance(attributes("currency")), attributes("rate").toDouble )
      rate.setDate(DateFormat.parse(attributes("date")))
      rate
    })
  }

  private def prepare() {
    httpGetStream[Unit](DataSourceUrl) {
      streamReader: BufferedReader => {
        parse(streamReader)
      }
    }
  }

  private def rateAttributes(): Map[String, String] = Map(
    "currency" -> "./@currency",
    "rate"     -> "./@rate",
    "date"     -> "/Envelope/Cube/Cube/@time"
  )

  def frequency() = (1l, TimeUnit.DAYS)

}