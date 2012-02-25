package sellstome.solr.schema

import sellstome.solr.util.SellstomeSolrTestCaseJ4
import org.junit.{Test, BeforeClass}
import org.scalatest.junit.{JUnitSuite, AssertionsForJUnit}

/** Companion object. */
object MoneyFieldComponentTest {

  /** Initializes a solr core */
  @BeforeClass def beforeClass() {
    SellstomeSolrTestCaseJ4.initCore("solrconfig.xml", "schema-money.xml")
  }

}

/**
 * Tests MoneyType
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MoneyFieldComponentTest extends SellstomeSolrTestCaseJ4
                              with AssertionsForJUnit
                              with JUnitSuite {

  @Test def testSchemaBasics() {
    val schema = testHelper.getCore().getSchema()

    val priceField = schema.getField("price")
    assert(priceField != null)
    assert(priceField.isPolyField())
    
    val priceFieldType = schema.getFieldType("price")
    assert(priceField.getType() == priceFieldType)

    val moneyType = schema.getFieldTypeByName("money")
    assert(moneyType != null)
    assert(moneyType.isInstanceOf[MoneyType])
    assert(moneyType.isPolyField())
  }

  @Test def testMoneyFieldType() {
    val core          = testHelper.getCore()
    val schema        = core.getSchema()
    val priceField    = schema.getField("price")
    assert(priceField != null)
    assert(priceField.isPolyField(), "Price field is not a poly field")

    val fieldType     = priceField.getType()
    assert(fieldType.isInstanceOf[MoneyType])

    val simplePrice   = List("100000","USD").reduceLeft(_+","+_)
    val indexedFields = fieldType.createFields(priceField, simplePrice, 1.0f)
    assert(indexedFields.length == 2)
  }

  /** Tests a search index with money data type field in the index */
  @Test def testSearching() {
    for (i <- 0 until 50) {
      assertU(adoc("id",i.toString(), "price", (i*100)+","+"EUR"))
    }
    assertU(commit())
    assertQ(req("fl", "*,score", "q", "*:*"), "//*[@numFound='50']")

    clearIndex()
  }

}
