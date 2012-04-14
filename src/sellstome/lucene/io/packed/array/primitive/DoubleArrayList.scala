package sellstome.lucene.io.packed.array.primitive

import gnu.trove.list.array.TDoubleArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DoubleArrayList extends PrimitiveList[Double] {
  private[this] lazy val delegate = new TDoubleArrayList()
  def add(value: Double): Boolean = delegate.add(value)
  def set(offset: Int, value: Double): Double = delegate.set(offset, value)
  def toArray(): Array[Double] = delegate.toArray()
}