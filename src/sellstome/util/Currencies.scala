package sellstome.solr.util

import java.util.Currency

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 03.02.12
 * Time: 9:18
 * Utility methods for working with currencies
 */
object Currencies {

  implicit def asJavaCurrency(code: String): Currency =
    Currency.getInstance(code)

  def apply(code: String): Currency = Currency.getInstance(code)

}
