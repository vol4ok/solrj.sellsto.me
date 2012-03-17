package sellstome.solr.schema.finance

import org.scalatest.FunSuite
import java.util.Currency
import org.apache.solr.common.SolrException
import org.apache.lucene.util.BytesRef
import sellstome.solr.util.Currencies
import sellstome.BaseUnitTest

/**
 * @author Alexander Zhugrov
 * @since 1.0
 */
class MoneyValueUnitTest extends BaseUnitTest {

  val USD = Currency.getInstance("USD")

  test("Test int value and USD currency") {
    val balance = MoneyValue.parse("1000,USD")
    expect(100000) (balance.getAmount)
    expect(USD) (balance.getCurrency)
  }
  
  test("Test valid float value and USD currency") {
    val balance = MoneyValue.parse("1000.1,USD")
    expect(100010) (balance.getAmount)
    expect(USD) (balance.getCurrency)
  }
  
  test("Test valid float value with high precision and USD currency") {
    val balance = MoneyValue.parse("1000.11,USD")
    expect(100011) (balance.getAmount)
    expect(USD) (balance.getCurrency)
  }
  
  test("Test invalid float value with high precision and USD currency") {
    try {
      val balance = MoneyValue.parse("1000.111,USD")
      fail("Should not reach here")
    }
    catch {
      case e:SolrException => Console.println("Catch Exception")
    }
  }
  
  test("Test valid float value and invalid currency") {
    try {
      val balance = MoneyValue.parse("1000,hckhnahcjkgcsa")
      fail("Should not reach here")
    } catch {
      case e: SolrException => Console.println("Catch Exception")
    }
  }
  
  test("Test invalid value") {
    try {
      val balance = MoneyValue.parse("shadkjhsakjhdklsa")
      fail("Should not reach here")
    } catch {
      case e: SolrException => Console.println("Catch Exception")
    }
  }
  
  test("Test normal case for low level parsing") {
    val value = MoneyValue.parse(new BytesRef("1000,USD"))
    expect( 100000 ) ( value._1 )
    expect( Currencies("USD") ) ( value._2 )
  }

}
