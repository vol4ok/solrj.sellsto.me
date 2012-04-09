package sellstome.lucene.io.packed.array.primitive

import gnu.trove.list.array.TShortArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class ShortArrayList extends PrimitiveList[Short] {
  private[this] lazy val delegate = new TShortArrayList()
  def add(value: Short): Boolean = delegate.add(value)
  def toArray(): Array[Short] = delegate.toArray()
}