package sellstome.search.solr.common

import org.scalatest.FunSuite
import java.io.StringReader

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 26.01.12
 * Time: 1:32
 * @test {XmlParser}
 */
class XmlParserUnitTest extends FunSuite with XmlParser {

  private val XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gesmes:Envelope xmlns:gesmes=\"http://www.gesmes.org/xml/2002-08-01\" " +
    "xmlns=\"http://www.ecb.int/vocabulary/2002-08-01/eurofxref\">\n\t<gesmes:subject>Reference rates</gesmes:subject>\n\t<gesmes:Sender>\n\t\t" +
    "<gesmes:name>European Central Bank</gesmes:name>\n\t</gesmes:Sender>\n\t<Cube>\n\t\t<Cube time='2012-01-25'>\n\t\t\t<Cube currency='USD' rate='1.2942'/>" +
    "\n\t\t\t<Cube currency='JPY' rate='101.02'/>\n\t\t\t<Cube currency='BGN' rate='1.9558'/>\n\t\t\t<Cube currency='CZK' rate='25.371'/>" +
    "\n\t\t\t<Cube currency='DKK' rate='7.4343'/>\n\t\t\t<Cube currency='GBP' rate='0.83205'/>\n\t\t\t<Cube currency='HUF' rate='298.38'/>" +
    "\n\t\t\t<Cube currency='LTL' rate='3.4528'/>\n\t\t\t<Cube currency='LVL' rate='0.6978'/>\n\t\t\t<Cube currency='PLN' rate='4.2966'/>" +
    "\n\t\t\t<Cube currency='RON' rate='4.3492'/>\n\t\t\t<Cube currency='SEK' rate='8.8506'/>\n\t\t\t<Cube currency='CHF' rate='1.2075'/>" +
    "\n\t\t\t<Cube currency='NOK' rate='7.6825'/>\n\t\t\t<Cube currency='HRK' rate='7.5728'/>\n\t\t\t<Cube currency='RUB' rate='39.7886'/>" +
    "\n\t\t\t<Cube currency='TRY' rate='2.3660'/>\n\t\t\t<Cube currency='AUD' rate='1.2367'/>\n\t\t\t<Cube currency='BRL' rate='2.2830'/>" +
    "\n\t\t\t<Cube currency='CAD' rate='1.3114'/>\n\t\t\t<Cube currency='CNY' rate='8.1713'/>\n\t\t\t<Cube currency='HKD' rate='10.0443'/>" +
    "\n\t\t\t<Cube currency='IDR' rate='11507.66'/>\n\t\t\t<Cube currency='ILS' rate='4.9027'/>\n\t\t\t<Cube currency='INR' rate='65.0270'/>" +
    "\n\t\t\t<Cube currency='KRW' rate='1461.59'/>\n\t\t\t<Cube currency='MXN' rate='17.0297'/>\n\t\t\t<Cube currency='MYR' rate='3.9903'/>" +
    "\n\t\t\t<Cube currency='NZD' rate='1.6064'/>\n\t\t\t<Cube currency='PHP' rate='55.825'/>\n\t\t\t<Cube currency='SGD' rate='1.6447'/>" +
    "\n\t\t\t<Cube currency='THB' rate='40.897'/>\n\t\t\t<Cube currency='ZAR' rate='10.4134'/>\n\t\t</Cube>\n\t</Cube>\n</gesmes:Envelope>"

  test("parse valid xml") {
    parse( new StringReader(XML) )
    val matchedNodes = getNodeList( "/Envelope/Cube/Cube/Cube" , true).get
    expect (33) (matchedNodes.getLength)
    var index = 0
    while(index < matchedNodes.getLength) {
      val node = matchedNodes.item(index)
      val currency = getNode("./@currency", node, true )
      val rates    = getNode("./@rate", node, true )
      val time     = getNode("/Envelope/Cube/Cube/@time", node, true)
      index = index + 1
    }
  }

}