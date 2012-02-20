package sellstome.solr.util

import org.apache.solr.SolrTestCaseJ4
import sellstome.util.Logging
import org.apache.solr.common.SolrException
import java.util.HashSet
import org.apache.solr.util.TestHarness
import org.apache.solr.common.params.CommonParams

/**
 * Contains a set of the utility methods for tests
 * @author Aliaksandr Zhuhrou
 * @since  1.0
 */
object SellstomeSolrTestCaseJ4 extends SolrTestCaseJ4
                               with Logging{

  /** Causes an exception matching the regex pattern to not be logged. */
  def ignoreException(pattern: String) {
    if (SolrException.ignorePatterns == null) SolrException.ignorePatterns = new HashSet[String]()
    SolrException.ignorePatterns.add(pattern)
  }

  var factoryProp: String         = null
  var testHelper: SolrTestHelper  = null

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
    factoryProp = System.getProperty("solr.directoryFactory")
    if (factoryProp == null) {
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
    SolrTestCaseJ4.solrConfig = TestHarness.createConfig(SolrTestCaseJ4.getSolrConfigFile())
    testHelper = new SolrTestHelper( SolrTestCaseJ4.dataDir.getAbsolutePath, SolrTestCaseJ4.solrConfig, SolrTestCaseJ4.getSchemaFile())
    SolrTestCaseJ4.h = testHelper
    SolrTestCaseJ4.lrf = testHelper.getRequestFactory("standard", 0, 20, CommonParams.VERSION, "2.2")
  }

}

/**
 * A JUnit4 Solr test case
 * Initializes a sellstome version of solr core
 * @author Aliaksandr Zhuhrou
 * @since  1.0
 */
class SellstomeSolrTestCaseJ4 extends SolrTestCaseJ4
                              with Logging {

  /** Returns a reference to a test helper */
  def testHelper = SellstomeSolrTestCaseJ4.testHelper

}