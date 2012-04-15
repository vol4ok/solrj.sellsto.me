package sellstome.collection.primitive

import gnu.trove.stack.array.TFloatArrayStack

/**
 * A wrapper for the [[gnu.trove.stack.array.TFloatArrayStack]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class FloatArrayStack extends PrimitiveStack[Float] {
  private[this] lazy val delegate = new TFloatArrayStack()
  def push(value: Float) { delegate.push(value) }
  def pop() = delegate.pop()
  def peek() = delegate.peek()
  def size() = delegate.size()
}
