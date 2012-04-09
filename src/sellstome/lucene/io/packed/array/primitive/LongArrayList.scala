package sellstome.lucene.io.packed.array.primitive

import gnu.trove.list.array.TLongArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class LongArrayList extends PrimitiveList[Long] {
  private[this] lazy val delegate = new TLongArrayList()
  def add(value: Long): Boolean = delegate.add(value)
  def toArray(): Array[Long] = delegate.toArray()
}