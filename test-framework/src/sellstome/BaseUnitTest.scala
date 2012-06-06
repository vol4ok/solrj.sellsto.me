package sellstome

import org.scalatest.FunSuite
import org.junit.Assert
import util.{NumberGeneratorComponent, Logging}
import scala.util.Random

//q: do you really ready to avoid the static state
//think on where is the best way to factor the shared state
object NumberGeneratorComponent {
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
                            with NumberGeneratorComponent
                            with Logging {

  /** a seed used for random creation */
  private[this] val _seed = randomSeed
  /** a new random generator */
  private[this] val _random = new java.util.Random(_seed)

  protected def assertArrayEqual[T](expected: Array[T], actual: Array[T])(implicit m: Manifest[T]) {
    if (m.erasure == classOf[Byte]) {
      Assert.assertArrayEquals(expected.asInstanceOf[Array[Byte]], actual.asInstanceOf[Array[Byte]])
    } else if (m.erasure == classOf[Short]) {
      Assert.assertArrayEquals(expected.asInstanceOf[Array[Short]], actual.asInstanceOf[Array[Short]])
    } else if (m.erasure == classOf[Int]) {
      Assert.assertArrayEquals(expected.asInstanceOf[Array[Int]], actual.asInstanceOf[Array[Int]])
    } else if (m.erasure == classOf[Long]) {
      Assert.assertArrayEquals(expected.asInstanceOf[Array[Long]], actual.asInstanceOf[Array[Long]])
    } else if (m.erasure == classOf[Float]) {
      val expectedFloat = expected.asInstanceOf[Array[Float]]
      val actualFloat   = actual.asInstanceOf[Array[Float]]
      Assert.assertEquals(expectedFloat.length, actualFloat.length)
      for (i <- 0 until expectedFloat.length) {
        Assert.assertEquals(java.lang.Float.floatToRawIntBits(expectedFloat(i)),
                            java.lang.Float.floatToRawIntBits(actualFloat(i)))
      }
    } else if (m.erasure == classOf[Double]) {
      val expectedDouble = expected.asInstanceOf[Array[Double]]
      val actualDouble   = actual.asInstanceOf[Array[Double]]
      Assert.assertEquals(expectedDouble.length, actualDouble.length)
      for (i <- 0 until expectedDouble.length) {
        Assert.assertEquals(java.lang.Double.doubleToRawLongBits(expectedDouble(i)),
                            java.lang.Double.doubleToRawLongBits(actualDouble(i)))
      }
    } else {
      ???
    }
  }

  /** get reference to currently used random generator */
  protected def random: java.util.Random = _random

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