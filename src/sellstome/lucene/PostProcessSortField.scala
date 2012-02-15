package sellstome.lucene

import org.apache.lucene.search.{FieldComparatorSource, SortField}
import javax.annotation.Nonnull


/**
 * Allows define post-processor that
 * can affect the final sorting order of given elements.
 * todo zhugrov a - how to define a post-processor semantics for a cases when we have sorting by multiple fields.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param field Name of field to sort by; cannot be <code>null</code>.
 * @param comparator Returns a comparator for sorting hits.
 * @param reverse True if natural order should be reversed.
 */
class PostProcessSortField(@Nonnull field: String, @Nonnull comparator: FieldComparatorSource, reverse: Boolean)
      extends SortField(field, comparator, reverse) {

}

/**
 * Defines a custom postprocessor
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class PostProcessor {

}