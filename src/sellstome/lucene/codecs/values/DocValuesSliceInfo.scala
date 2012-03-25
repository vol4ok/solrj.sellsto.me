package sellstome.lucene.codecs.values

import javax.annotation.Nonnull

/**
 * Information about a given doc values slice.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfo(@Nonnull name: String) {

  /** Name of given slice info */
  @Nonnull
  def getName(): String = name

}
