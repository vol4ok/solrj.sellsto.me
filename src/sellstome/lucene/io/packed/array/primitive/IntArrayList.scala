package sellstome.lucene.io.packed.array.primitive

import gnu.trove.list.array.TIntArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class IntArrayList extends PrimitiveList[Int] {
  private[this] lazy val delegate = new TIntArrayList()
  def add(value: Int): Boolean = delegate.add(value)
  def toArray(): Array[Int] = delegate.toArray()
}