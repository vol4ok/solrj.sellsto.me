package scala

import org.scalatest.FunSuite

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 27.01.12
 * Time: 11:51
 * Some tests that related to scala language features.
 */
class LanguageFeaturesUnitTest extends FunSuite {

  test("String interpolation") {
    val a = 10
    val b = 20
    val sip = s"The value of a=$a and the value of b=$b"
    Console.println(sip)
  }

}