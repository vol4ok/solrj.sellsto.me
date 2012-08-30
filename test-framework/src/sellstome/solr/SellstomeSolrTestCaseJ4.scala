package sellstome.solr

import org.apache.solr.common.SolrException
import java.util.HashSet
import org.apache.solr.util.TestHarness
import org.apache.solr.{JSONTestUtil, SolrTestCaseJ4}
import org.apache.solr.common.params.{ModifiableSolrParams, SolrParams, CommonParams}
import org.apache.solr.request.SolrQueryRequest
import org.scalatest.junit.JUnitSuite
import sellstome.util.Logging

/**
 * Contains a set of the utility methods for tests
 * @author Aliaksandr Zhuhrou
 * @since  1.0
 */
object SellstomeSolrTestCaseJ4 extends SolrTestCaseJ4
with Logging {

  /**Causes an exception matching the regex pattern to not be logged. */
  def ignoreException(pattern: String) {
    if (SolrException.ignorePatterns == null) SolrException.ignorePatterns = new HashSet[String]()
    SolrException.ignorePatterns.add(pattern)
  }

  var testHelper: SolrTestHelper = null

  /**
   * Call initCore in @BeforeClass to instantiate a solr core in your test class.
   * deleteCore will be called for you via SolrTestCaseJ4 @AfterClass
   */
  def initCore(config: String, schema: String) {
    initCore(config, schema, SolrTestCaseJ4.TEST_HOME())
  }

  /**
   * Call initCore in @BeforeClass to instantiate a solr core in your test class.
   * deleteCore will be called for you via SolrTestCaseJ4 @AfterClass
   */
  def initCore(config: String, schema: String, solrHome: String) {
    SolrTestCaseJ4.configString = config
    SolrTestCaseJ4.schemaString = schema
    if (solrHome != null) {
      System.setProperty("solr.solr.home", solrHome)
    }
    initCore()
  }

  def initCore() {
    info("####initCore")
    ignoreException("ignore_exception")
    SolrTestCaseJ4.factoryProp = System.getProperty("solr.directoryFactory")
    if (SolrTestCaseJ4.factoryProp == null) {
      System.setProperty("solr.directoryFactory", "solr.RAMDirectoryFactory")
    }
    if (SolrTestCaseJ4.dataDir == null) {
      SolrTestCaseJ4.createTempDir()
    }
    System.setProperty("solr.test.sys.prop1", "propone")
    System.setProperty("solr.test.sys.prop2", "proptwo")
    val configFile: String = SolrTestCaseJ4.getSolrConfigFile()
    if (configFile != null) {
      createCore()
    }
    info("####initCore end")
  }

  def createCore() {
    SolrTestCaseJ4.solrConfig = TestHarness.createConfig(SolrTestCaseJ4.testSolrHome, SolrTestCaseJ4.getSolrConfigFile())
    testHelper = new SolrTestHelper(SolrTestCaseJ4.dataDir.getAbsolutePath, SolrTestCaseJ4.solrConfig, SolrTestCaseJ4.getSchemaFile())
    SolrTestCaseJ4.h = testHelper
    SolrTestCaseJ4.lrf = testHelper.getRequestFactory("standard", 0, 20, CommonParams.VERSION, "2.2")
  }

  def deleteCore() {
    SolrTestCaseJ4.deleteCore()

  }

}

/**
 * A JUnit4 Solr test case
 * Initializes a sellstome version of solr core
 * @author Aliaksandr Zhuhrou
 * @since  1.0
 */
abstract class SellstomeSolrTestCaseJ4 extends SolrTestCaseJ4
                              with Logging
                              with JUnitSuite
{

  /**
   * Shuts down the test harness, and makes the best attempt possible
   * to delete dataDir, unless the system property "solr.test.leavedatadir"
   * is set.
   */
  def deleteCore() = SellstomeSolrTestCaseJ4.deleteCore()

  def initCore(config: String, schema: String):Unit = SellstomeSolrTestCaseJ4.initCore(config, schema)

  /** Returns a reference to a test helper */
  def testHelper = SellstomeSolrTestCaseJ4.testHelper

  /**
   * Validates an update XML String is successful
   */
  def assertU(update: String) {
    assertU(null, update)
  }

  /**
   * Validates an update XML String is successful
   */
  def assertU(message: String, update: String) {
    SolrTestCaseJ4.assertU(message, update)
  }

  /**
   * Validates an update XML String failed
   */
  def assertFailedU(update: String) {
    assertFailedU(null, update)
  }

  /**
   * Validates an update XML String failed
   */
  def assertFailedU(message: String, update: String) {
    SolrTestCaseJ4.assertFailedU(message, update)
  }

  /**
   * Generates a simple &lt;add&gt;&lt;doc&gt;... XML String with no options
   *
   * @param fieldsValues 0th and Even numbered args are fields names odds are field values.
   * @see #add
   * @see #doc
   */
  def adoc(fieldsValues: String*): String = {
    SolrTestCaseJ4.add(SolrTestCaseJ4.doc(fieldsValues: _*))
  }

  /** Send JSON update commands */
  def updateJ(json: String, args: SolrParams) = SolrTestCaseJ4.updateJ(json, args)

  /**
   * Validates a query matches some JSON test expressions using the default double delta tollerance.
   * @see JSONTestUtil#DEFAULT_DELTA
   * @see #assertJQ(SolrQueryRequest,double,String...)
   */
  def assertJQ(req: SolrQueryRequest, tests: String*) {
    SolrTestCaseJ4.assertJQ(req, JSONTestUtil.DEFAULT_DELTA, tests: _*)
  }

  /**
   * Validates a query matches some JSON test expressions and closes the
   * query. The text expression is of the form path:JSON.  To facilitate
   * easy embedding in Java strings, the JSON can have double quotes
   * replaced with single quotes.
   * <p>
   * Please use this with care: this makes it easy to match complete
   * structures, but doing so can result in fragile tests if you are
   * matching more than what you want to test.
   * </p>
   * @param req Solr request to execute
   * @param delta tolerance allowed in comparing float/double values
   * @param tests JSON path expression + '==' + expected value
   */
  def assertJQ(req: SolrQueryRequest, delta: Double, tests: String*) {
    SolrTestCaseJ4.assertJQ(req, delta, tests: _*)
  }

  /**Makes sure a query throws a SolrException with the listed response code */
  def assertQEx(message: String, req: SolrQueryRequest, code: Int) {
    SolrTestCaseJ4.assertQEx(message, req, code)
  }

  def assertQEx(message: String, req: SolrQueryRequest, code: SolrException.ErrorCode) {
    SolrTestCaseJ4.assertQEx(message, req, code)
  }

  /**Validates a query matches some XPath test expressions and closes the query */
  def assertQ(req: SolrQueryRequest, tests: String*) {
    SolrTestCaseJ4.assertQ(null, req, tests: _*)
  }

  /**Validates a query matches some XPath test expressions and closes the query */
  def assertQ(message: String, req: SolrQueryRequest, tests: String*) {
    SolrTestCaseJ4.assertQ(message, req, tests: _*)
  }

  /**
   * @see TestHarness#optimize
   */
  def optimize(args: String*): String = SolrTestCaseJ4.optimize(args: _*)

  /**
   * @see TestHarness#commit
   */
  def commit(args: String*): String = SolrTestCaseJ4.commit(args: _*)

  /**
   * Generates a &lt;delete&gt;... XML string for an ID
   * @see TestHarness#deleteById
   */
  def delI(id: String): String = SolrTestCaseJ4.delI(id)

  /**
   * Generates a &lt;delete&gt;... XML string for an query
   * @see TestHarness#deleteByQuery
   */
  def delQ(q: String): String = SolrTestCaseJ4.delQ(q)

  def params(params: String*): ModifiableSolrParams = SolrTestCaseJ4.params(params: _*)

  /**Causes an exception matching the regex pattern to not be logged. */
  def ignoreException(pattern: String) {
    SolrTestCaseJ4.ignoreException(pattern)
  }

  def resetExceptionIgnores() {
    SolrTestCaseJ4.resetExceptionIgnores()
  }

  /**
   * Generates a SolrQueryRequest
   */
  def req(q: String*): SolrQueryRequest = SolrTestCaseJ4.req(q: _*)


}