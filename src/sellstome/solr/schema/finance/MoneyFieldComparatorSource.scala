package sellstome.solr.schema.finance

import org.apache.lucene.index.AtomicReaderContext
import sellstome.solr.service.finance.ICurrencyExchangeRateService
import org.apache.lucene.util.Bits
import org.apache.lucene.search._
import org.apache.lucene.search.FieldValueHitQueue.Entry
import util.Sorting

/**
 * A factory class that instantiates the MoneyFieldComparator.
 * I suspect that first iteration would not be performance wise.
 * I take the LongComparator as the base for this class.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MoneyFieldComparatorSource extends FieldComparatorSource {

  def newComparator(primaryField: String, numHits: Int, sortPos: Int, reversed: Boolean) = {
    val missingValue = if (reversed) Long.MinValue else Long.MaxValue
    new MoneyFieldComparator(numHits, primaryField, missingValue)
  }

  /**
   * Implement comparator that allows sorting for money values based on the latest exchange rates.
   * @param numHits - ?? todo zhugrov a - define the meaning of this field
   * @param primaryField a indexed field name that stores amount in base currency
   * @param missingValue a value that would be virtually assigned to a field in case if given doc does not contain the field.
   *  Should be irrelevant to our current schema as price is the required field.
   */
  class MoneyFieldComparator(numHits: Int, primaryField: String, missingValue: Long) extends FieldComparator[Long] {

    private val values: Array[Long] = new Array[Long](numHits)
    /**Values encoded as long for a given indexed part of field */
    private var currentReaderValues: Array[Long] = null
    private var bottom: Long = 0L
    private var docsWithField: Bits = null
    private val primaryParser = FieldCache.NUMERIC_UTILS_LONG_PARSER
    /**allows directly manipulate this queue content */
    private var queue: FieldValueHitQueue[Entry] = null

    def compare(firstSlot: Int, secondSlot: Int): Int = {
      val firstValue = values(firstSlot)
      val secondValue = values(secondSlot)
      return Ordering.Long.compare(firstValue, secondValue)
    }

    def setBottom(slot: Int) {
      this.bottom = values(slot)
    }

    def setPriorityQueue(queue: FieldValueHitQueue[Entry]) {
      this.queue = queue
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
      docsWithField = FieldCache.DEFAULT.getDocsWithField(context.reader, primaryField)
      if (docsWithField.isInstanceOf[Bits.MatchAllBits]) {
        docsWithField = null
      }
      return this
    }

    def value(slot: Int): Long = values(slot)

    /** todo zhugrov a - investigate the implementation */
    def compareDocToValue(doc: Int, value: Long) = ???
  }

}
