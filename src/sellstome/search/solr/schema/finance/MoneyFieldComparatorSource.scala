package sellstome.search.solr.schema.finance

import org.apache.lucene.index.AtomicReaderContext
import sellstome.search.solr.service.finance.ICurrencyExchangeRateService
import org.apache.lucene.search.FieldComparator.NumericComparator
import org.apache.lucene.search.{FieldComparator, FieldCache, FieldComparatorSource}
import org.apache.lucene.util.{BytesRef, Bits}
import sellstome.search.solr.common.NotImplementedException
import org.apache.lucene.util.packed.PackedInts

/**
 * A factory class that instantiates the MoneyFieldComparator.
 * I suspect that first iteration would not be performance wise.
 * I take the LongComparator as the base for this class.
 * @param exchangeRatesService - allows fetch the latest exchange rates
 * @param secondaryField       - the field that stores the original currency amount
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MoneyFieldComparatorSource(exchangeRatesService: ICurrencyExchangeRateService, secondaryField: Option[String]) extends FieldComparatorSource {
  
  def newComparator(primaryField: String, numHits: Int, sortPos: Int, reversed: Boolean) = {
    val missingValue = if (reversed) Long.MinValue else Long.MaxValue
    new MoneyFieldComparator(numHits, primaryField, secondaryField,
                             missingValue, exchangeRatesService)
  }

  /**
   * Implement comparator that allows sorting for money values based on the latest exchange rates.
   * @param numHits - ?? todo zhugrov a - define the meaning of this field
   * @param primaryField a indexed field name that stores amount in base currency
   * @param secondaryField a indexed field that stores amount and original currency. Not defined in case if a schema field is not indexed.
   * @param missingValue a value that would be virtually assigned to a field in case if given doc does not contain the field.
   *  Should be irrelevant to our current schema as price is the required field.
   * @param exchangeRatesService provides a latest exchange rates information
   */
  class MoneyFieldComparator(numHits: Int, primaryField: String, secondaryField: Option[String],
                             missingValue: Long, exchangeRatesService: ICurrencyExchangeRateService) extends FieldComparator[Long] {

    private val values: Array[Long] = new Array[Long](numHits)
    /** Values encoded as long for a given indexed part of field */
    private var currentReaderValues: Array[Long] = null
    private var bottom: Long = 0L
    private var docsWithField: Bits = null
    private val primaryParser = FieldCache.NUMERIC_UTILS_LONG_PARSER

    /**
     * As you probably know from MoneyType we use two lucene IndexedField in order to store money value.
     * The first field stores a long amount value in base currency. It used for fast primary lookup.
     * And the second value encodes money amount in original currency as long number (@see sellstome.search.solr.schema.finance.MoneyValue#convertToMinorCurrency)
     * and ISO4217 Currency code.
     * So we need to fetch the value of second field and manually decode it as money value.
     */
    private var reader:PerSegmentMoneyFieldReader = null

    def compare(firstSlot: Int, secondSlot: Int): Int = {
      val firstValue  = values(firstSlot)
      val secondValue = values(secondSlot)
      return Ordering.Long.compare(firstValue, secondValue)
    }

    def setBottom(slot: Int) {
      this.bottom = values(slot)
    }

    def compareBottom(docId: Int): Int = {
      var secondValue = currentReaderValues(docId)
      if (docsWithField != null && secondValue == 0 && !docsWithField.get(docId)) {
        secondValue = missingValue
      }
      return Ordering.Long.compare(bottom, secondValue)
    }

    def copy(slot: Int, doc: Int) {
      var v2 = currentReaderValues(doc)
      if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
        v2 = missingValue
      }
      values(slot) = v2
    }

    def setNextReader(context: AtomicReaderContext): MoneyFieldComparator = {
      currentReaderValues = FieldCache.DEFAULT.getLongs(context.reader, primaryField, primaryParser, true)
      docsWithField       = FieldCache.DEFAULT.getDocsWithField(context.reader, primaryField)
      if (secondaryField.isDefined)                      { /* termsIndex    = FieldCache.DEFAULT.getTermsIndex(context.reader, secondaryField.get) */ }
      
      if (docsWithField.isInstanceOf[Bits.MatchAllBits]) { docsWithField = null }
      throw new NotImplementedException()
      //return this
    }

    def value(slot: Int): Long = values(slot)

  }

  /**
   * Reads lucene field as money value.
   * Tightly integrated with comparator API.
   */
  private abstract class PerSegmentMoneyFieldReader(numHits: Int, termsIndex: FieldCache.DocTermsIndex) {

    /** Stores references to a given field in a given document value. */
    protected val values: Array[BytesRef] = new Array[BytesRef](numHits)

    /** Returns value for a given slot */
    def value(slot: Int): BytesRef

  }
  
  private final class   ByteOrdMoneyFieldReader(numHits: Int, termsIndex: FieldCache.DocTermsIndex, readerOrds: Array[Byte])
                extends PerSegmentMoneyFieldReader(numHits, termsIndex) {

    def value(slot: Int) = throw new NotImplementedException()

  }
  
  private final class   ShortOrdMoneyFieldReader(numHits: Int, termsIndex: FieldCache.DocTermsIndex, readerOrds: Array[Short])
                extends PerSegmentMoneyFieldReader(numHits, termsIndex) {

    def value(slot: Int) = throw new NotImplementedException()

  }
  
  private final class   IntOrdMoneyFieldReader(numHits: Int, termsIndex: FieldCache.DocTermsIndex, readerOrds: Array[Int])
                extends PerSegmentMoneyFieldReader(numHits, termsIndex) {
    
    def value(slot: Int) = throw new NotImplementedException()
  
  }
  
  private final class   AnyOrdMoneyReader(numHits: Int, termsIndex: FieldCache.DocTermsIndex, readerOrds: PackedInts.Reader)
                extends PerSegmentMoneyFieldReader(numHits, termsIndex) {

    def value(slot: Int) = null

  }
}