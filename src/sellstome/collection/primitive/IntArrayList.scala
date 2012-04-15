package sellstome.collection.primitive

import gnu.trove.list.array.TIntArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class IntArrayList extends PrimitiveList[Int] {
  private[this] lazy val delegate = new TIntArrayList()
  def add(value: Int): Boolean = delegate.add(value)
  def set(offset: Int, value: Int): Int = delegate.set(offset, value)
  def toArray(): Array[Int] = delegate.toArray()
}