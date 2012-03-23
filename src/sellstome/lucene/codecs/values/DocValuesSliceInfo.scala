package sellstome.lucene.codecs.values

/**
 * Information about a given doc values slice.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfo(name: String) {
  /** Name of given slice info */
  def getName(): String = name

}
