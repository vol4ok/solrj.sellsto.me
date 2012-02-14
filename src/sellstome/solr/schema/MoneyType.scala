package sellstome.solr.schema

import finance.{MoneyFieldComparatorSource, MoneyValue, MoneyValueSource}
import org.slf4j.{LoggerFactory, Logger}
import org.apache.lucene.document.FieldType
import org.apache.solr.search.function.ValueSourceRangeFilter
import org.apache.lucene.search.{SortField, Query}
import org.apache.solr.common.SolrException
import org.apache.solr.schema.{SchemaField, IndexSchema, AbstractSubTypeFieldType}
import org.apache.solr.response.TextResponseWriter
import org.apache.solr.search.{QParser, SolrConstantScoreQuery}
import org.apache.lucene.index.IndexableField
import com.google.common.collect.Lists
import org.apache.lucene.queries.function.ValueSource
import sellstome.solr.service.finance.CurrencyExchangeRatesService
import sellstome.solr.util.Currencies
import beans.BooleanBeanProperty
import org.apache.lucene.index.FieldInfo.IndexOptions

object MoneyType {
  /** logger instance */
  private[schema] val log: Logger = LoggerFactory.getLogger(classOf[MoneyType])
  /** todo zhugrov a - think how to refactor it via dependency injection pattern */
  val ExchangeRatesService = new CurrencyExchangeRatesService()
  /** base currency */
  val BaseCurrency = Currencies("EUR")
  /** is debug parameter name for a given SchemaField */
  private[schema] val DebugEnabled = "debug"
}

/** Field type for support of monetary values. */
class MoneyType extends AbstractSubTypeFieldType {

  /** whenever we print the debug information */
  @BooleanBeanProperty
  var debugEnabled = false

  protected override def init(schema: IndexSchema, args: java.util.Map[String, String]) {
    super.init(schema, args)
    val debugEnabled = args.get(MoneyType.DebugEnabled)
    if (debugEnabled != null) {
      this.debugEnabled = debugEnabled.toBoolean
      args.remove(MoneyType.DebugEnabled)
    }
    createSuffixCache(1)
  }

  override def isPolyField: Boolean = true

  override def createFields(field: SchemaField, externalVal: AnyRef, boost: Float): Array[IndexableField] = {
    val f: Array[IndexableField] = new Array[IndexableField]((if (field.indexed) 2 else 0) + (if (field.stored) 1 else 0))
    if (field.indexed) {
      val value = MoneyType.ExchangeRatesService.convertCurrency(MoneyValue.parse(externalVal.toString), MoneyType.BaseCurrency)
      f(0) = subField(field, 0).createField(String.valueOf(value.getAmount), boost)
    }
    if (field.stored) {
      val customType = new FieldType()
      customType.setStored(true)
      customType.setIndexed(true)
      customType.setIndexOptions(IndexOptions.DOCS_ONLY)
      f(f.length - 1) = createField(field.getName, externalVal.toString, customType, boost)
    }
    return f
  }

  override def getFieldQuery(parser: QParser, field: SchemaField, externalVal: String): Query = {
    return getRangeQuery(parser, field, externalVal, externalVal, true, true)
  }

  override def getRangeQuery(parser: QParser, field: SchemaField, lowerBoundValue: String, upperBoundValue: String, minInclusive: Boolean, maxInclusive: Boolean): Query = {
    //todo zhugrov a - reimplement this method
    val lower: MoneyValue = MoneyValue.parse(lowerBoundValue)
    val upper: MoneyValue = MoneyValue.parse(upperBoundValue)
    if (!(lower.getCurrency  == upper.getCurrency)) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Cannot parse range query " + lowerBoundValue + " to " + upperBoundValue + ": range queries only supported when upper and lower bound have same currency.")
    }
    return new SolrConstantScoreQuery(new ValueSourceRangeFilter(getValueSource(field, parser) , lower.getAmount + "", upper.getAmount + "", minInclusive, maxInclusive))
  }

  override def getValueSource(field: SchemaField, parser: QParser): ValueSource = {
    //todo zhugrov a - study hard the implementation of this method
    val indexField = subField(field, 0)
    return new MoneyValueSource(field, Lists.newArrayList(indexField.getType().getValueSource(indexField, parser)))
  }

  def getSortField(field: SchemaField, reverse: Boolean): SortField = new SortField(   subField(field, 0).getName(),
                                                                                       //todo zhugrov a - if field is not stored then secondary field should be None
                                                                                       new MoneyFieldComparatorSource(MoneyType.ExchangeRatesService, Option(field.getName())),
                                                                                       reverse)

  /**
   * It never makes sense to create a single field, so make it impossible to happen
   */
  override def createField(field: SchemaField, value: AnyRef, boost: Float): IndexableField = {
    throw new UnsupportedOperationException("LatLonType uses multiple fields. Field=" + field.getName)
  }

  /** How to use this method to deserialize the corresponding indexable field */
  def write(writer: TextResponseWriter, name: String, field: IndexableField) = {
    writer.writeStr(name, field.stringValue, false)
  }

}