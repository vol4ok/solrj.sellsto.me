package sellstome.search.solr.response

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/29/11
 * Time: 7:14 PM
 * Tests what could be achieved with var args
 */
object TestVarargs extends App {

  varargs()
  varargs(Map("one" -> "two"))

  def varargs(params: Map[Any,Any]*) {
    Console.println("test")
  }

}