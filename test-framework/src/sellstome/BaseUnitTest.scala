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

  /** int value between 0(inclusive) and the specified value (exclusive) */
  def nextInt(max: Int): Int = random.nextInt(max)
  /** generates uniformly distributed long value */
  def nextLong(): Long = random.nextLong()
  /** an uniformly distributed double value between 0.0 and 1.0 */
  def nextDouble(): Double = random.nextDouble()
  /** an uniformly distributed boolean value */
  def nextBoolean(): Boolean = random.nextBoolean()

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
