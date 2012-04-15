package sellstome.collection.primitive

import gnu.trove.stack.array.TIntArrayStack

/**
 * A wrapper for the [[gnu.trove.stack.array.TIntArrayStack]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class IntArrayStack extends PrimitiveStack[Int] {
  private[this] lazy val delegate = new TIntArrayStack()
  def push(value: Int) { delegate.push(value) }
  def pop() = delegate.pop()
  def peek() = delegate.peek()
  def size() = delegate.size()
}
