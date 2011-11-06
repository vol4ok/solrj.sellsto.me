package sellstome.search.solr
import java.math.BigDecimal

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 16.10.11
 * Time: 2:17
 * To change this template use File | Settings | File Templates.
 */
object Test extends App {
  val one = new BigDecimal(2.00)
  val two = new BigDecimal(1.90)
  Console.println(one.subtract(two))
}