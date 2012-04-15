package sellstome.collection.primitive

object PrimitiveStack {
  /** creates a new stack instance for type T */
  def forType[T](implicit m: Manifest[T]): PrimitiveStack[T] = {
    if (m.erasure == classOf[Byte]) {
      return new ByteArrayStack().asInstanceOf[PrimitiveStack[T]]
    } else if (m.erasure == classOf[Short]) {
      return new ShortArrayStack().asInstanceOf[PrimitiveStack[T]]
    } else if (m.erasure == classOf[Int]) {
      return new IntArrayStack().asInstanceOf[PrimitiveStack[T]]
    } else if (m.erasure == classOf[Long]) {
      return new LongArrayStack().asInstanceOf[PrimitiveStack[T]]
    } else if (m.erasure == classOf[Float]) {
      return new FloatArrayStack().asInstanceOf[PrimitiveStack[T]]
    } else if (m.erasure == classOf[Double]) {
      return new DoubleArrayStack().asInstanceOf[PrimitiveStack[T]]
    } else {
      throw new IllegalArgumentException(s"Unsupported erasure type: ${m.erasure}")
    }
  }
}

/**
 * A generic facade for trove collections
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait PrimitiveStack[T] {
  /**
   * Pushes the value onto the top of the stack.
   * @param value a value
   */
  def push(value: T)
  /**
   * Removes and returns the value at the top of the stack.
   * @return a value
   */
  def pop(): T
  /**
   * Returns the value at the top of the stack.
   * @return a value
   */
  def peek(): T
  /** Returns the current depth of the stack. */
  def size(): Int
}