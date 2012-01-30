package sellstome.search.solr.schema

import finance.{MoneyValue, MoneyValueSource}
import org.slf4j.{LoggerFactory, Logger}
import java.util.Currency
import org.apache.lucene.document.FieldType
import org.apache.solr.search.function.ValueSourceRangeFilter
import org.apache.lucene.search.{SortField, Query}
import org.apache.solr.common.SolrException
import java.io.IOException
import org.apache.solr.schema.{SchemaField, IndexSchema, AbstractSubTypeFieldType}
import org.apache.solr.response.TextResponseWriter
import org.apache.solr.search.{QParser, SolrConstantScoreQuery}
import org.apache.lucene.index.IndexableField
import com.google.common.collect.Lists
import org.apache.lucene.queries.function.ValueSource


object MoneyType {
  /** logger instance */
  private val log: Logger = LoggerFactory.getLogger(classOf[MoneyType])
}

/**
 * Field type for support of monetary values.
 * <p>
 * See <a href="http://wiki.apache.org/solr/MoneyFieldType">http://wiki.apache.org/solr/MoneyFieldType</a>
 */
class MoneyType extends AbstractSubTypeFieldType {

  protected override def init(schema: IndexSchema, args: java.util.Map[String, String]) {
    super.init(schema, args)
    createSuffixCache(3)
  }

  override def isPolyField: Boolean = true

  override def createFields(field: SchemaField, externalVal: AnyRef, boost: Float): Array[IndexableField] = {
    val f: Array[IndexableField] = new Array[IndexableField]((if (field.indexed) 2 else 0) + (if (field.stored) 1 else 0))
    if (field.indexed) {
      val value = MoneyValue.parse(externalVal.toString)
      f(0) = subField(field, 0).createField(String.valueOf(value.getAmount), boost)
      f(1) = subField(field, 1).createField(value.getCurrency.getCurrencyCode, boost)
    }
    if (field.stored) {
      val customType = new FieldType()
      customType.setStored(true)
      f(f.length - 1) = createField(field.getName, externalVal.toString, customType, boost)
    }
    return f
  }

  override def getFieldQuery(parser: QParser, field: SchemaField, externalVal: String): Query = {
    return getRangeQuery(parser, field, externalVal, externalVal, true, true)
  }

  override def getRangeQuery(parser: QParser, field: SchemaField, lowerBoundValue: String, upperBoundValue: String, minInclusive: Boolean, maxInclusive: Boolean): Query = {
    val lower: MoneyValue = MoneyValue.parse(lowerBoundValue)
    val upper: MoneyValue = MoneyValue.parse(upperBoundValue)
    if (!(lower.getCurrency  == upper.getCurrency)) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Cannot parse range query " + lowerBoundValue + " to " + upperBoundValue + ": range queries only supported when upper and lower bound have same currency.")
    }
    return new SolrConstantScoreQuery(new ValueSourceRangeFilter(getValueSource(field, parser) , lower.getAmount + "", upper.getAmount + "", minInclusive, maxInclusive))
  }

  override def getValueSource(field: SchemaField, parser: QParser): ValueSource = {
      val vs = Lists.newArrayList[ValueSource]
      for ( i <- 0 until 2) {
        val sub = subField(field, i)
        vs.add(sub.getType().getValueSource(sub, parser))
      }
      return new MoneyValueSource(field, vs)
  }

  def getSortField(field: SchemaField, reverse: Boolean): SortField = {
    try {
      return (getValueSource(field, null)).getSortField(reverse)
    }
    catch {
      case e: IOException => {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e)
      }
    }
  }

  /**
   * It never makes sense to create a single field, so make it impossible to happen
   */
  override def createField(field: SchemaField, value: AnyRef, boost: Float): IndexableField = {
    throw new UnsupportedOperationException("LatLonType uses multiple fields.  field=" + field.getName)
  }

  def write(writer: TextResponseWriter, name: String, field: IndexableField) = {
    writer.writeStr(name, field.stringValue, false)
  }

}