package sellstome.lucene.io.packed.array.primitive

/**
 * A generic facade for trove collections
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait PrimitiveList[T] {
  /**
   * Adds <tt>val</tt> to the end of the list, growing as needed.
   * @param value an value
   * @return true if the list was modified by the add operation
   */
  def add(value: T): Boolean

  /**
   * Copies the contents of the list into a native array.
   * @return a native array
   */
  def toArray(): Array[T]

}