package sellstome.lucene.util

import org.apache.lucene.util.IOUtils
import sellstome.BaseUnitTest
import java.io.Closeable

/**
 * Tests the [[org.apache.lucene.util.IOUtils]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class IOUtilsUnitTest extends BaseUnitTest {

  test("test on null handling") {
    val test: Closeable = null
    IOUtils.close(test)
    IOUtils.closeWhileHandlingException(test)
  }

}
