package sellstome.search.solr.response

import java.util.Currency
import Console._
import sellstome.search.solr.service.finance.ExchangeRate

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 24.01.12
 * Time: 13:06
 * Test how currency api works in java
 */
object TestCurrency extends App {

  val cur = Currency.getInstance("USD")
  println(cur)

  val rate = new ExchangeRate(Currency.getInstance("RUB"), Currency.getInstance("USD"), 1.023)

}