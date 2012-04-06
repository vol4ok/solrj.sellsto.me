package common

import sellstome.BaseUnitTest

/**
 * Some tests that related to scala language features.
 * @author Aliaksandr Zhuhrou
 */
class LanguageFeaturesUnitTest extends BaseUnitTest {

  test("Option") {
    var result = None.map[Option[Boolean]]((number: Int) =>
      if (number > 4) Some(true) else None
    ).flatMap[Boolean](result =>
      if (result.isDefined) Some(result.get) else None
    )
    Console.println("result: " + result)
  }

  test("Equals") {
    val one = new String(Array('a', 'b', 'c'))
    val two = new String(Array('a', 'b', 'c'))
    Console.println(one == two)
  }

  test("'new' keyword syntax") {
    val collection = new {
      val one = "one"
      val second = "second"
    }
    Console.println(collection.getClass())
  }

  test("null value and string extension format methot") {
    Console.println("do you aware of null effect: %s".format(null))
  }

  test("overrloaded methods") {
    val testStr: String = "one"
    val testAny: Any = testStr
    print(testStr)
    print(testAny)
  }

  def print(test: String) {
    Console.println("String: " + test)
  }

  def print(test: Any) {
    Console.println("Any:" + test)
  }

}