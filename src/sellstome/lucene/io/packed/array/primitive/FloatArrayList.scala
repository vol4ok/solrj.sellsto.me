package sellstome.lucene.io.packed.array.primitive

import gnu.trove.list.array.TFloatArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class FloatArrayList extends PrimitiveList[Float] {
  private[this] lazy val delegate = new TFloatArrayList()
  def add(value: Float): Boolean = delegate.add(value)
  def toArray(): Array[Float] = delegate.toArray()
}