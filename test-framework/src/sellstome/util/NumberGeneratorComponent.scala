package sellstome.util

import reflect.ClassTag

/**
 * Adds the ability to generate basic random sequences
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait NumberGeneratorComponent {

  val numGen = new NumberGenerator()

  class NumberGenerator() {

    /** generates a byte number between 0(inclusive) and Byte.MaxValue(exclusive) */
    def nextByte(): Byte = random.nextInt(Byte.MaxValue.toInt).toByte
    /** generates a short number between 0(inclusive) and Short.MaxValue(exclusive)  */
    def nextShort(): Short = random.nextInt(Short.MaxValue.toInt).toShort
    /** int value between 0(inclusive) and the max specified value (exclusive) */
    def nextInt(max: Int): Int = random.nextInt(max)
    /** an int value between low(inclusive) and the max (exclusive) */
    def nextIntInRange(low: Int, max: Int): Int = low + random.nextInt(max)
    /** int value between 0(inclusive) and the Int.MaxValue value (exclusive) */
    def nextInt(): Int = random.nextInt(Int.MaxValue)
    /** generates uniformly distributed long value */
    def nextLong(): Long = random.nextLong()
    /** Returns the next pseudorandom, uniformly distributed float value
     *  between 0.0 and 1.0 from this random number generator's sequence. */
    def nextFloat(): Float = random.nextFloat()
    /** an uniformly distributed double value between 0.0 and 1.0 */
    def nextDouble(): Double = random.nextDouble()
    /** an uniformly distributed boolean value */
    def nextBoolean(): Boolean = random.nextBoolean()

    /** generates a random number of a given type */
    def nextNumber[T](implicit erasureTag: ClassTag[T]): T = {
      if (erasureTag.runtimeClass == classOf[Byte]) {
        return nextByte().asInstanceOf[T]
      } else if (erasureTag.runtimeClass == classOf[Short]) {
        return nextShort().asInstanceOf[T]
      } else if (erasureTag.runtimeClass == classOf[Int]) {
        return nextInt().asInstanceOf[T]
      } else if (erasureTag.runtimeClass == classOf[Long]) {
        return nextLong().asInstanceOf[T]
      } else if (erasureTag.runtimeClass == classOf[Float]) {
        return nextFloat().asInstanceOf[T]
      } else if (erasureTag.runtimeClass == classOf[Double]) {
        return nextDouble().asInstanceOf[T]
      } else {
        throw new IllegalArgumentException(s"not supported erasure type: ${erasureTag.runtimeClass}")
      }
    }

    /**
     * Creates a new primitive arrays.
     * May contain a duplicates values.
     */
    def newNumericArray[T](size: Int)(implicit classTag: ClassTag[T]) : Array[T] = {
      val array = classTag.newArray(size)
      if (classTag.runtimeClass == classOf[Int]) {
        for (i <- 0 until size) {
          array.update(i, nextInt(1000).asInstanceOf[T])
        }
      } else if (classTag.runtimeClass == classOf[Long]) {
        for (i <- 0 until size) {
          array.update(i, nextLong().asInstanceOf[T])
        }
      } else if (classTag.runtimeClass == classOf[Short]) {
        for (i <- 0 until size) {
          array.update(i, nextInt(1000).toShort.asInstanceOf[T])
        }
      } else if (classTag.runtimeClass == classOf[Byte]) {
        for (i <- 0 until size) {
          array.update(i, nextInt(100).toByte.asInstanceOf[T])
        }
      } else if (classTag.runtimeClass == classOf[Float]) {
        for (i <- 0 until size) {
          array.update(i, (nextDouble() * 1000).toFloat.asInstanceOf[T])
        }
      } else if (classTag.runtimeClass == classOf[Double]) {
        for (i <- 0 until size) {
          array.update(i, (nextDouble() * 1000).asInstanceOf[T])
        }
      }
      return array
    }

  }

  /** get reference to currently used random generator */
  protected def random: java.util.Random

}
