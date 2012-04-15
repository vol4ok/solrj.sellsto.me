package sellstome.collection.primitive

import gnu.trove.stack.array.TByteArrayStack

/**
 * An wrapper for the [[gnu.trove.stack.array.TByteArrayStack]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class ByteArrayStack extends PrimitiveStack[Byte] {
  private[this] lazy val delegate = new TByteArrayStack()
  def push(value: Byte) { delegate.push(value) }
  def pop() = delegate.pop()
  def peek() = delegate.peek()
  def size() = delegate.size()
}
