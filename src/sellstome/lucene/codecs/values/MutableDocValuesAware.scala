package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues.Type

/**
 * A trait that has a basic methods
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait MutableDocValuesAware {

  /** a list of doc values types that this field consumer supports. */
  protected val SupportedTypes = List(Type.FIXED_INTS_8,
                                      Type.FIXED_INTS_16,
                                      Type.FIXED_INTS_32,
                                      Type.FIXED_INTS_64,
                                      Type.FLOAT_32,
                                      Type.FLOAT_64)

  /**
   * Checks if this codec support given [[org.apache.lucene.index.DocValues.Type]]
   * @param docValuesType a given type
   * @return whenever we supports this type
   */
  protected def isSupportedType(docValuesType: Type): Boolean = SupportedTypes.contains(docValuesType)


}
