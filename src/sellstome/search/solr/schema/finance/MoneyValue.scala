package sellstome.search.solr.schema.finance

import org.apache.solr.common.SolrException
import java.util.Currency
import com.google.common.base.Joiner

object MoneyValue {
  
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
    if (!externalVal.contains(",")) throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Invalid external val - " + externalVal)

    val amountAndCode: Array[String] = externalVal.split(",")
    val amount   = amountAndCode(0)
    val currency = Currency.getInstance(amountAndCode(1))

    if (currency == null) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Invalid currency code " + currency)
    }

    try {
      return new MoneyValue(java.lang.Long.parseLong(amount), currency)
    }
    catch {
      case e: NumberFormatException => {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e)
      }
    }
  }

  /**
   * Performs a currency conversion & unit conversion.
   *
   * @param exchangeRates      Exchange rates to apply.
   * @param sourceCurrencyCode The source currency code.
   * @param sourceAmount       The source amount.
   * @param targetCurrencyCode The target currency code.
   * @return The converted indexable units after the exchange rate and currency fraction digits are applied.
   */
//  def convertAmount(exchangeRates: ExchangeRateProvider, sourceCurrencyCode: String, sourceAmount: Long, targetCurrencyCode: String): Long = {
//    var exchangeRate: Double = exchangeRates.getExchangeRate(sourceCurrencyCode, targetCurrencyCode)
//    return convertAmount(exchangeRate, sourceCurrencyCode, sourceAmount, targetCurrencyCode)
//  }

  /**
   * Performs a currency conversion & unit conversion.
   *
   * @param exchangeRate         Exchange rate to apply.
   * @param sourceFractionDigits The fraction digits of the source.
   * @param sourceAmount         The source amount.
   * @param targetFractionDigits The fraction digits of the target.
   * @return The converted indexable units after the exchange rate and currency fraction digits are applied.
   */
  def convertAmount(exchangeRate: Double, sourceFractionDigits: Int, sourceAmount: Long, targetFractionDigits: Int): Long = {
    var digitDelta: Int = targetFractionDigits - sourceFractionDigits
    var value: Double = (sourceAmount.asInstanceOf[Double] * exchangeRate)
    if (digitDelta != 0) {
      if (digitDelta < 0) {
        {
          var i: Int = 0
          while (i < -digitDelta) {
            {
              value *= 0.1
            }
            ({
              i += 1; i
            })
          }
        }
      }
      else {
        {
          var i: Int = 0
          while (i < digitDelta) {
            {
              value *= 10.0
            }
            ({
              i += 1; i
            })
          }
        }
      }
    }
    return value.asInstanceOf[Long]
  }

  /**
   * Performs a currency conversion & unit conversion.
   *
   * @param exchangeRate       Exchange rate to apply.
   * @param sourceCurrencyCode The source currency code.
   * @param sourceAmount       The source amount.
   * @param targetCurrencyCode The target currency code.
   * @return The converted indexable units after the exchange rate and currency fraction digits are applied.
   */
  def convertAmount(exchangeRate: Double, sourceCurrencyCode: String, sourceAmount: Long, targetCurrencyCode: String): Long = {
    if (targetCurrencyCode == sourceCurrencyCode) {
      return sourceAmount
    }
    var sourceFractionDigits: Int = Currency.getInstance(sourceCurrencyCode).getDefaultFractionDigits
    var targetCurrency: Currency = Currency.getInstance(targetCurrencyCode)
    var targetFractionDigits: Int = targetCurrency.getDefaultFractionDigits
    return convertAmount(exchangeRate, sourceFractionDigits, sourceAmount, targetFractionDigits)
  }
}

/**
 * Represents a Money field value, which includes a long amount and ISO currency code.
 */
class MoneyValue(amount: Long, currency: Currency) {

  /**
   * The amount of the MoneyValue.
   *
   * @return The amount.
   */
  def getAmount: Long = amount

  /**
   * The ISO currency code of the MoneyValue.
   *
   * @return The currency code.
   */
  def getCurrency: Currency = currency

  override def toString: String = Joiner.on(',').join(amount, currency.getCurrencyCode)

}