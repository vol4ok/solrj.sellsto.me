package sellstome.util

import util.Random

//q: do you really ready to avoid the static state
//think on where is the best way to factor the shared state
object NumberGeneratorComponent {
  /** a random number generator for seed values */
  val SeedRand = new Random()
}

/**
 * Adds the ability to generate basic random sequences
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait NumberGeneratorComponent {

  val numGen = new NumberGenerator()

  class NumberGenerator {
    /** a seed used for random creation */
    val seed = randomSeed
    /** a new random generator */
    val random = new Random(seed)

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
    def nextNumber[T](implicit m: Manifest[T]): T = {
      if (m.erasure == classOf[Byte]) {
        return nextByte().asInstanceOf[T]
      } else if (m.erasure == classOf[Short]) {
        return nextShort().asInstanceOf[T]
      } else if (m.erasure == classOf[Int]) {
        return nextInt().asInstanceOf[T]
      } else if (m.erasure == classOf[Long]) {
        return nextLong().asInstanceOf[T]
      } else if (m.erasure == classOf[Float]) {
        return nextFloat().asInstanceOf[T]
      } else if (m.erasure == classOf[Double]) {
        return nextDouble().asInstanceOf[T]
      } else {
        throw new IllegalArgumentException(s"not supported erasure type: ${m.erasure}")
      }
    }

    /**
     * Creates a new primitive arrays.
     * May contain a duplicates values.
     */
    def newNumberArray[T](size: Int)(implicit m: Manifest[T]) : Array[T] = {
      val array = m.newArray(size)
      if (m.erasure == classOf[Int]) {
        for (i <- 0 until size) {
          array.update(i, nextInt(1000).asInstanceOf[T])
        }
      } else if (m.erasure == classOf[Long]) {
        for (i <- 0 until size) {
          array.update(i, nextLong().asInstanceOf[T])
        }
      } else if (m.erasure == classOf[Short]) {
        for (i <- 0 until size) {
          array.update(i, nextInt(1000).toShort.asInstanceOf[T])
        }
      } else if (m.erasure == classOf[Byte]) {
        for (i <- 0 until size) {
          array.update(i, nextInt(100).toByte.asInstanceOf[T])
        }
      } else if (m.erasure == classOf[Float]) {
        for (i <- 0 until size) {
          array.update(i, (nextDouble() * 1000).toFloat.asInstanceOf[T])
        }
      } else if (m.erasure == classOf[Double]) {
        for (i <- 0 until size) {
          array.update(i, (nextDouble() * 1000).asInstanceOf[T])
        }
      }
      return array
    }

    /** @throws NumberFormatException  if the system param value string does not parsable value */
    protected def randomSeed: Long = {
      val seedStr = System.getProperty("tests.seed", "random")
      return if (seedStr == "random") {
        seedGenerator.nextLong()
      } else {
        seedStr.toLong
      }
    }

    /** gets a generator used for generating a seed values */
    protected def seedGenerator: Random = NumberGeneratorComponent.SeedRand

  }

}
