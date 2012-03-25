package sellstome.control

import sellstome.BaseUnitTest
import java.io.IOException

/**
 * Tests [[sellstome.control.trysupress]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class TrySupressUnitTest extends BaseUnitTest {

  test("normal case") {
    val result = trysupress {
      2 + 2
    }
    assert(result.get == 4)
  }

  test("return inside closure") {
    assert(calculate() == 10)
  }

  protected def calculate(): Int = {
    trysupress {
      return 10
    }
    return 0
  }

  test("supress error") {
    var result = trysupress {
      throw new IOException("Could not open file.")
    }
    assert(result == None)
  }

}
