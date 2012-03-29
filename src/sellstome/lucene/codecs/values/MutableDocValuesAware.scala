package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues.Type

/** Companion object */
object MutableDocValuesAware {

  /** a list of doc values types that this field consumer supports. */
  val SupportedTypes = List(Type.FIXED_INTS_64, Type.VAR_INTS)

}

/**
 * A trait that has a basic methods
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait MutableDocValuesAware {

  /** companion object */
  private[this] val companion = MutableDocValuesAware

  /**
   * Checks if this codec support given [[org.apache.lucene.index.DocValues.Type]]
   * @param docValuesType a given type
   * @return whenever we supports this type
   */
  protected def isSupportedType(docValuesType: Type): Boolean = companion.SupportedTypes.contains(docValuesType)


}
