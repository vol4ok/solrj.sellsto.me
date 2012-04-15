package sellstome.collection.primitive

import gnu.trove.list.array.TFloatArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class FloatArrayList extends PrimitiveList[Float] {
  private[this] lazy val delegate = new TFloatArrayList()
  def add(value: Float): Boolean = delegate.add(value)
  def set(offset: Int, value: Float): Float = delegate.set(offset, value)
  def toArray(): Array[Float] = delegate.toArray()
}