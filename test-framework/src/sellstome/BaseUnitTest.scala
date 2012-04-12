package sellstome

import org.scalatest.FunSuite
import scala.util.Random
import util.Logging

object BaseUnitTest {
  /** a name of the random seed parameter */
  val RandomSeedParamName = "tests.seed"
  /** a random number generator for seed values */
  val SeedRand = new Random()
}

/**
 * Base class that contains a common methods for writing a unit tests
 * Ads randomized capabilities to unit tests
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class BaseUnitTest extends FunSuite
                            with Logging {

  val seed = getRandomSeed()

  val random = new Random(seed)
  /** generates a byte number between 0(inclusive) and Byte.MaxValue(exclusive) */
  def nextByte(): Byte = random.nextInt(Byte.MaxValue.toInt).toByte
  /** generates a short number between 0(inclusive) and Short.MaxValue(exclusive)  */
  def nextShort(): Short = random.nextInt(Short.MaxValue.toInt).toShort
  /** int value between 0(inclusive) and the max specified value (exclusive) */
  def nextInt(max: Int): Int = random.nextInt(max)
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

   /** @throws NumberFormatException  if the system param value string does not parsable value */
  protected def getRandomSeed(): Long = {
    val seedStr = System.getProperty("tests.seed", "random")
    return if (seedStr == "random") {
      getSeedGenerator().nextLong()
    } else {
      seedStr.toLong
    }
  }

  /** gets a generator used for generating a seed values */
  protected def getSeedGenerator(): Random = BaseUnitTest.SeedRand

}
