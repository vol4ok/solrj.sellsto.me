package sellstome.collection.primitive

import gnu.trove.stack.array.TLongArrayStack

/**
 * A wrapper for the [[gnu.trove.stack.array.TLongArrayStack]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class LongArrayStack extends PrimitiveStack[Long] {
  private[this] lazy val delegate = new TLongArrayStack()
  def push(value: Long) { delegate.push(value) }
  def pop() = delegate.pop()
  def peek() = delegate.peek()
  def size() = delegate.size()
}
