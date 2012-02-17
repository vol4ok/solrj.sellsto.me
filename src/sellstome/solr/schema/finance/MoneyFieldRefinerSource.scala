package sellstome.solr.schema.finance

import org.apache.lucene.search.FieldValueHitQueue
import sellstome.lucene.{SortRefinerComparator, SortRefinerComparatorSource}
import org.apache.lucene.search.FieldValueHitQueue.Entry
import sellstome.solr.service.finance.ICurrencyExchangeRateService
import org.apache.lucene.index.{FieldInfo, StoredFieldVisitor, IndexReader}
import org.apache.lucene.index.StoredFieldVisitor.Status
import java.util.Currency
import javax.annotation.{Nullable, Nonnull}

/**
 * Creates a refiner for sorting by a money field.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param storedFieldName a name of stored field
 */
class MoneyFieldRefinerSource(@Nonnull storedFieldName: String,
                              @Nonnull exchangeRateService: ICurrencyExchangeRateService,
                              @Nonnull baseCurrency: Currency) extends SortRefinerComparatorSource[FieldValueHitQueue.Entry] {

  /** Creates a new refiner for money type field */
  def newRefiner(indexReader: IndexReader, reversed: Boolean) = new MoneyFieldRefiner(indexReader, storedFieldName, reversed, exchangeRateService, baseCurrency)

}

/** Creates a new refiner for a money type field. */
class MoneyFieldRefiner(@Nonnull indexReader: IndexReader, @Nonnull storedFieldName: String, @Nonnull reversed: Boolean,
                        @Nonnull exchangeRateService: ICurrencyExchangeRateService, @Nonnull baseCurrency: Currency)
      extends SortRefinerComparator[FieldValueHitQueue.Entry](indexReader) {

  /** Compares two pre-ordered hits. Uses original (amount,currency) value and latest exchange rates */
  override def compare(@Nullable firstEntry: AnyRef, @Nullable secondEntry: AnyRef):Int = {
    //todo zhugrov a - cleanup this code
    if (firstEntry == null && secondEntry == null) return  0
    if (firstEntry == null && secondEntry != null) return -1
    if (firstEntry != null && secondEntry == null) return  1
    val firstValue  = { val visitor = new MoneyFieldVisitor(storedFieldName); indexReader.document(firstEntry.asInstanceOf[Entry].doc,  visitor); visitor.getValue() }
    val secondValue = { val visitor = new MoneyFieldVisitor(storedFieldName); indexReader.document(secondEntry.asInstanceOf[Entry].doc, visitor); visitor.getValue() }
    val firstValueConveted    = exchangeRateService.convertCurrency(firstValue,  baseCurrency)
    val secondValueConverted  = exchangeRateService.convertCurrency(secondValue, baseCurrency)
    val compare = firstValueConveted.getAmount.compareTo( secondValueConverted.getAmount )
    return if (reversed) (-1) * compare else compare
  }

}

/**
 * todo zhugrov a - is possible to use some kind of cache?
 * @param storedFieldName
 */
private class MoneyFieldVisitor(@Nonnull storedFieldName: String) extends StoredFieldVisitor {
  
  private var value: String = null

  override def stringField(fieldInfo: FieldInfo, value: String) {
    this.value = value
  }

  /** Checks if we should process a given field. */
  def needsField(fieldInfo: FieldInfo): Status = {
    if (value == null) {
      if (fieldInfo.name equals storedFieldName) {
        return Status.YES
      } else {
        return Status.NO
      }
    } else {
      return Status.STOP
    }
  }

  /** Returns a money value for a given document. */
  def getValue(): MoneyValue = {
    if (value == null) throw new IllegalStateException("Have not found value for field: "+storedFieldName+" or unitialized")
    MoneyValue.parse(value)
  }
  
}