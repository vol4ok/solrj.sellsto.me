package common

import sellstome.BaseUnitTest
import java.nio.ByteOrder

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

  test("null value and string interpolation") {
    val test: Any = null
    Console.println(s"Test: ${test}")
  }

  test("overrloaded methods") {
    val testStr: String = "one"
    val testAny: Any = testStr
    print(testStr)
    print(testAny)
  }

  test("byte order on underlying platform") {
    Console.println(ByteOrder.nativeOrder())
  }

  test("hex values converter") {
    Console.println(java.lang.Integer.toBinaryString(0xFF))
  }

  test("bitwise operations in scala") {
    val number = 8147387603249568337l
    Console.println(java.lang.Long.toBinaryString(number))
    Console.println(java.lang.Long.toBinaryString(number).length)
    Console.println(toBinaryString((number       ).toInt))
    Console.println(toBinaryString((number >>   8).toInt))
    Console.println(toBinaryString((number >>  16).toInt))
    Console.println(toBinaryString((number >>  24).toInt))
    Console.println(toBinaryString((number >>  32).toInt))
    Console.println(toBinaryString((number >>  40).toInt))
    Console.println(toBinaryString((number >>  48).toInt))
    Console.println(toBinaryString((number >>  56).toInt))
  }

  protected def toBinaryString(num: Int): String = {
    val rawStr = java.lang.Integer.toBinaryString(num)
    if (rawStr.length > 8)
      rawStr.substring(rawStr.length - 8)
    else
      rawStr
  }

  def print(test: String) {
    Console.println("String: " + test)
  }

  def print(test: Any) {
    Console.println("Any:" + test)
  }

}