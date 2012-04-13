package sellstome

import org.scalatest.FunSuite
import org.junit.Assert
import util.{NumberGeneratorComponent, Logging}

/**
 * Base class that contains a common methods for writing a unit tests
 * Ads randomized capabilities to unit tests
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class BaseUnitTest extends FunSuite
                            with NumberGeneratorComponent
                            with Logging {

  protected def assertArrayEqual[T](expected: Array[T], actual: Array[T])(implicit m: Manifest[T]) {
    if (m.erasure == classOf[Byte]) {
      Assert.assertArrayEquals(expected.asInstanceOf[Array[Byte]], actual.asInstanceOf[Array[Byte]])
    } else if (m.erasure == classOf[Int]) {
      Assert.assertArrayEquals(expected.asInstanceOf[Array[Int]], actual.asInstanceOf[Array[Int]])
    } else {
      ???
    }
  }

}