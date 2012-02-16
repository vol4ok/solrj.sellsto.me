package sellstome.lucene

import javax.annotation.Nonnull
import java.util.Comparator
import org.apache.lucene.search.{FieldValueHitQueue, FieldComparatorSource, SortField}
import org.apache.lucene.index.IndexReader


/**
 * Allows define post-processor that
 * can affect the final sorting order of given elements.
 * todo zhugrov a - how to define a post-processor semantics for a cases when we have sorting by multiple fields.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param field Name of field to sort by; cannot be <code>null</code>.
 * @param comparatorSource Returns a comparator source that instantiates a comparator for sorting hits
 * @param refinerSource A factory for creating a refiner for the result of a search. Usefull in case if sort order depends on real-time signals so
 *        it allows us make approximate sort based on raw static signals and refine it with in-memory real-time data.
 * @param reverse True if natural order should be reversed.
 */
class PostProcessSortField(@Nonnull field: String, @Nonnull comparatorSource: FieldComparatorSource,
                           @Nonnull refinerSource: SortRefinerComparatorSource[FieldValueHitQueue.Entry], reverse: Boolean)
      extends SortField(field, comparatorSource, reverse) {


  /** Returns a refiner instance */
  @Nonnull
  def createRefiner(indexReader: IndexReader): SortRefinerComparator[FieldValueHitQueue.Entry] = refinerSource.newRefiner(indexReader, reverse)

}

/**
 * Defines a custom postprocess refinement comparator
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class SortRefinerComparator[Entry <: FieldValueHitQueue.Entry](@Nonnull indexReader: IndexReader) extends Comparator[Entry] {

  /** Compares two entries in SellstomeFieldValueHitQueue */
  def compare(firstEntry: Entry, secondEntry: Entry): Int

}

abstract class SortRefinerComparatorSource[Entry <: FieldValueHitQueue.Entry] {

  /**
   * Creates a new refiner for the given indexReader
   * @param indexReader - A lucene index reader. Should be topMost one. Allows implement access to given field
   */
  def newRefiner(@Nonnull indexReader: IndexReader, reversed: Boolean): SortRefinerComparator[Entry]

}