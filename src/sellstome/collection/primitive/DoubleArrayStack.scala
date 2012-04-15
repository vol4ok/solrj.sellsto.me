package sellstome.collection.primitive

import gnu.trove.stack.array.TDoubleArrayStack

/**
 * A wrapper for the [[gnu.trove.stack.array.TDoubleArrayStack]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DoubleArrayStack extends PrimitiveStack[Double] {
  private[this] lazy val delegate = new TDoubleArrayStack()
  def push(value: Double) { delegate.push(value) }
  def pop() = delegate.pop()
  def peek() = delegate.peek()
  def size() = delegate.size()
}