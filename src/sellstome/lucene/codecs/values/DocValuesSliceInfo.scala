package sellstome.lucene.codecs.values

import javax.annotation.Nonnull

/**
 * Information about a given doc values slice.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfo(@Nonnull val name: String) {

  override def hashCode() = name.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case other: DocValuesSliceInfo  => this.getClass == other.getClass && this.name == other.name
    case     _                      => false
  }

}
