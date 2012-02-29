package sellstome.solr.schema

import finance.{MoneyFieldRefinerSource, MoneyFieldComparatorSource, MoneyValue, MoneyValueSource}
import org.slf4j.{LoggerFactory, Logger}
import org.apache.lucene.document.FieldType
import org.apache.solr.common.SolrException
import org.apache.solr.response.TextResponseWriter
import org.apache.solr.search.QParser
import org.apache.lucene.index.IndexableField
import com.google.common.collect.Lists
import org.apache.lucene.queries.function.ValueSource
import sellstome.solr.service.finance.CurrencyExchangeRatesService
import sellstome.solr.util.Currencies
import beans.BooleanBeanProperty
import org.apache.lucene.index.FieldInfo.IndexOptions
import org.apache.solr.common.SolrException.ErrorCode
import sellstome.lucene.PostProcessSortField
import org.apache.lucene.search.{NumericRangeQuery, SortField, Query}
import MoneyType._
import javax.annotation.Nonnull
import org.apache.solr.schema._

object MoneyType {
  /** logger instance */
  private[schema] val log: Logger = LoggerFactory.getLogger(classOf[MoneyType])
  /** todo zhugrov a - think how to refactor it via dependency injection pattern */
  val ExchangeRateService = new CurrencyExchangeRatesService()
  /** base currency */
  val BaseCurrency = Currencies("EUR")
  /** is debug parameter name for a given SchemaField */
  private[schema] val DebugEnabled = "debug"
}

/** Field type for support of monetary values. */
class MoneyType extends AbstractSubTypeFieldType {

  /** whenever we print the debug information */
  @BooleanBeanProperty
  protected var debugEnabled = false

  /** The trie field precision that used for indexing the raw money amount value */
  protected var rawValuePrecisionStep = TrieField.DEFAULT_PRECISION_STEP


  protected override def init(schema: IndexSchema, args: java.util.Map[String, String]) {
    super.init(schema, args)
    val debugEnabled = args.get(MoneyType.DebugEnabled)
    if (debugEnabled != null) {
      this.debugEnabled = debugEnabled.toBoolean
      args.remove(MoneyType.DebugEnabled)
    }
    createSuffixCache(1)
  }


  /**
   * Q: should we cache a given value?
   * @return a precision step for a given support field
   */
  @inline
  private def getRawValuePrecisionStep(field: SchemaField): Int = subField(field, 0)
                                                                          .getType.asInstanceOf[TrieLongField]
                                                                          .getPrecisionStep()

  override def isPolyField: Boolean = true

  override def createFields(field: SchemaField, externalVal: AnyRef, boost: Float): Array[IndexableField] = {
    val f: Array[IndexableField] = new Array[IndexableField]((if (field.indexed) 1 else 0) + (if (field.stored) 1 else 0))
    if (field.indexed) {
      val value = ExchangeRateService.convertCurrency(MoneyValue.parse(externalVal.toString), MoneyType.BaseCurrency)
      f(0) = subField(field, 0).createField(String.valueOf(value.getAmount), boost)
    }
    if (field.stored) {
      val customType = new FieldType()
      customType.setStored(true)
      customType.setIndexed(true)
      customType.setIndexOptions(IndexOptions.DOCS_ONLY)
      f(f.length - 1) = createField(field.getName, externalVal.toString, customType, boost)
    } else {
      throw new SolrException(ErrorCode.SERVER_ERROR, "A money type field should be stored in order for the sort refinement to work corectly.")
    }
    return f
  }


  override def getFieldQuery(parser: QParser, field: SchemaField, externalVal: String): Query = {
    return getRangeQuery(parser, field, externalVal, externalVal, true, true)
  }


  /**
   * todo zhugrov a - test the implementation of this method.
   * @return a range query that used the raw numeric indexed value in base currency.
   */
  override def getRangeQuery(parser: QParser, field: SchemaField, lowerBoundValue: String, upperBoundValue: String, minInclusive: Boolean, maxInclusive: Boolean): Query = {
    val lower = MoneyValue.parse(lowerBoundValue)
    val upper = MoneyValue.parse(upperBoundValue)
    if (lower.getCurrency  != upper.getCurrency) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Cannot parse range query " + lowerBoundValue + " to " + upperBoundValue + ": range queries only supported when upper and lower bound have same currency.")
    }
    val lowerRaw = ExchangeRateService.convertCurrency(lower, MoneyType.BaseCurrency).getAmount
    val upperRaw = ExchangeRateService.convertCurrency(upper, MoneyType.BaseCurrency).getAmount

    return NumericRangeQuery.newLongRange(subField(field, 0).getName(),
                                          getRawValuePrecisionStep(field),
                                          lowerRaw, upperRaw, minInclusive, maxInclusive);
  }




  override def getValueSource(field: SchemaField, parser: QParser): ValueSource = {
    //todo zhugrov a - study hard the implementation of this method
    val indexField = subField(field, 0)
    return new MoneyValueSource(field, Lists.newArrayList(indexField.getType().getValueSource(indexField, parser)))
  }



  /** @return Returns the SortField instance that should be used to sort fields. Also performs refinemets of given sorts results. */
  def getSortField(field: SchemaField, reverse: Boolean): SortField = new PostProcessSortField( subField(field, 0).getName(),
                                                                                                new MoneyFieldComparatorSource(),
                                                                                                new MoneyFieldRefinerSource(field.getName(), MoneyType.ExchangeRateService, MoneyType.BaseCurrency),
                                                                                                reverse)


  /**
   * It never makes sense to create a single field, so make it impossible to happen
   */
  override def createField(field: SchemaField, value: AnyRef, boost: Float): IndexableField = {
    throw new UnsupportedOperationException("LatLonType uses multiple fields. Field=" + field.getName)
  }



  /** How to use this method to deserialize the corresponding indexable field */
  def write(writer: TextResponseWriter, name: String, field: IndexableField) {
    writer.writeStr(name, field.stringValue, false)
  }

}