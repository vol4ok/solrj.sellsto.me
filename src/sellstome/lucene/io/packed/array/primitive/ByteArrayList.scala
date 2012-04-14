package sellstome.lucene.io.packed.array.primitive

import gnu.trove.list.array.TByteArrayList

/**
 * A wrapper around trove primitives
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class ByteArrayList extends PrimitiveList[Byte] {
  private[this] lazy val delegate = new TByteArrayList()
  def add(value: Byte): Boolean = delegate.add(value)
  def set(offset: Int, value: Byte): Byte = delegate.set(offset, value)
  def toArray(): Array[Byte] = delegate.toArray()
}