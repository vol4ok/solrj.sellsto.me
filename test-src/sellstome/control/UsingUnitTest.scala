package sellstome.control

import org.easymock.EasyMock
import org.easymock.EasyMock._
import Console._
import java.io.Closeable
import sellstome.BaseUnitTest

/**
 * @author Alexander Zhugrov
 * @since 1.0
 */
class UsingUnitTest extends BaseUnitTest {

  test("Normal case") {
    val closeable = newCloseable()
    using(closeable) {
      reader =>
        println("Do something with Reader")
    }
    verify(closeable)
  }

  test("Test exception case") {
    val closeable = newCloseable()
    try {
      using(closeable) {
        reader =>
          throw new RuntimeException("This is anticipated exception")
      }
      fail()
    } catch {
      case e: RuntimeException => println("Intercept anticipated exception")
    }
    verify(closeable)
  }

  private def newCloseable(): Closeable = {
    val closeable = createMock(classOf[Closeable])
    EasyMock.expect(closeable.close()).once()
    replay(closeable)
    return closeable
  }

}
