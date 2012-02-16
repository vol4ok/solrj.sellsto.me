package sellstome.solr.response.json.converters

import org.apache.solr.schema.FieldType
import org.apache.lucene.index.IndexableField
import sellstome.solr.schema.MoneyType
import org.json.JSONObject
import sellstome.solr.schema.finance.MoneyValue

/**
 * Converts a money stored value to a json representation.
 * @author Aliaksandr Zhuhrou
 */
object MoneySolrFieldConverter extends SolrField2JsonConverter {
  def toJson(fieldType: FieldType, indexableField: IndexableField): JSONObject = fieldType match {
    case moneyType: MoneyType => {
      val moneyValue = MoneyValue.parse(indexableField.stringValue())
      val price = new JSONObject()
      price.put("amount", moneyValue.getAmount)
      price.put("currency", moneyValue.getCurrency.getCurrencyCode())
      if (moneyType.isDebugEnabled()) {
        price.put("amountInBaseCurrency", MoneyType.ExchangeRateService.convertCurrency(moneyValue, MoneyType.BaseCurrency))
      }
      return price
    }
    case _ => throw new IllegalArgumentException("This converter doesn't support a given fieldType: " + fieldType.getClass())
  }
}
