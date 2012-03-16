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

  test("Option") {
    var result = None.map[Option[Boolean]]( (number: Int) =>
      if (number > 4) Some(true) else None
    ).flatMap[Boolean]( result =>
       if (result.isDefined) Some(result.get) else None
    )
    Console.println("result: "+result)
  }

  test("Equals") {
    val one = new String(Array('a','b','c'))
    val two = new String(Array('a','b','c'))
    Console.println( one == two )
  }

}