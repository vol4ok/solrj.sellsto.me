package sellstome.search.solr.schema.finance

import java.util.Currency
import com.google.common.base.Joiner
import org.apache.solr.common.SolrException
import javax.annotation.Nonnull
import sellstome.search.solr.common.{SellstomeSolrComponent, trysolr, NotImplementedException}
import org.apache.lucene.search.FieldCache.Parser
import org.apache.lucene.util.BytesRef

/**
 * Represents a Money field value, which includes a long amount and ISO currency code.
 */
class MoneyValue(amount: Long, currency: Currency) {

  /** @return The amount in minor currency units. */
  def getAmount: Long = amount

  /** @return The currency. */
  def getCurrency: Currency = currency

  /** @return a String representation of this value. */
  override def toString: String = Joiner.on(',').join(amount, currency.getCurrencyCode)

}

/**An interface to parse doubles from document fields. */
trait  MoneyParser extends Parser {
  /** Returns a money value representation of this field's value. */
  def parse(term: BytesRef): (Long, Currency)
}

/** Contains an utility methods for dealing with money in string representation. */
object MoneyValue extends SellstomeSolrComponent with MoneyParser {

  /**
   * Constructs a new money value by parsing the specific input.
   * <p/>
   * Money values are expected to be in the format &lt;amount&gt;,&lt;currency code&gt;,
   * for example, "500,USD" would represent 5 U.S. Dollars.
   * <p/>
   * If no currency code is specified, the default is assumed.
   *
   * @param externalVal The value to parse.
   * @return The parsed MoneyValue.
   */
  def parse(externalVal: String): MoneyValue = {
    ensure(externalVal.count( _ == ',') == 1, "Invalid external val - " + externalVal, SolrException.ErrorCode.BAD_REQUEST)
    val amountStr   = externalVal.split(",")(0)
    val currencyStr = externalVal.split(",")(1)
    val currency    = trysolr { Currency.getInstance(currencyStr) }
    return new MoneyValue(convertToMinorCurrency(amountStr, currency), currency)
  }

  /**
   * Returns a money value representation of this field's value.
   * This method is implemented to be performance wise.
   * todo zhugrov a - this implementation requires a performance testing
   * @return a pair of values. the first value represent a amount in a given currency and the second
   * represent a ISO currency
   */
  def parse(term: BytesRef): (Long, Currency) = {
    val stored = term.utf8ToString()
    val amountAndCurrency = stored.split(",")
    val currency = Currency.getInstance(amountAndCurrency(1))
    return (convertToMinorCurrency(amountAndCurrency(0), currency), currency)
  }

  /**
   * The 4217ISO Standard also defines a relationship between a minor and major currency unit.
   * The only problem with it that the standard only defines a order of magnitude for a minor currency.
   * We use this method in order to convert a double value to long representation.
   * It solves two problems =>
   *  1. For performance reasons it is better to store currencies as integer
   *  2. It also solves precision problems.
   * @param amountStr - a given amount string. Can't be null.
   * @param currency - a given currency. Can't be null.
   */
  private def convertToMinorCurrency(@Nonnull amountStr: String, @Nonnull currency: Currency): Long = {
    if (currency.getDefaultFractionDigits == 0) {
      return trysolr {
        amountStr.toLong
      }
    } else {
      return trysolr {
        val amount = new java.math.BigDecimal(amountStr)
        ensure(amount.scale() <= currency.getDefaultFractionDigits,
            "Invalid amount part for an external val: "+amountStr, SolrException.ErrorCode.BAD_REQUEST)
        amount.setScale(currency.getDefaultFractionDigits).unscaledValue().longValue();
      }
    }
  }

}
