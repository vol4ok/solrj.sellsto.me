package sellstome.collection.primitive

import gnu.trove.stack.array.TShortArrayStack

/**
 * A wrapper for the [[gnu.trove.stack.array.TShortArrayStack]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class ShortArrayStack extends PrimitiveStack[Short] {
  private[this] lazy val delegate = new TShortArrayStack()
  def push(value: Short) { delegate.push(value) }
  def pop() = delegate.pop()
  def peek() = delegate.peek()
  def size() = delegate.size()
}
