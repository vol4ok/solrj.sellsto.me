package sellstome.solr.common

import org.scalatest.FunSuite
import org.easymock.EasyMock
import org.easymock.EasyMock._
import Console._
import java.io.Closeable

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 05.02.12
 * Time: 13:57
 *
 */
class UsingUnitTest extends FunSuite {
  
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
