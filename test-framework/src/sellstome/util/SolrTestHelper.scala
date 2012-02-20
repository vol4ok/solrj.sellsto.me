package sellstome.solr.util

import org.apache.solr.util.TestHarness
import org.apache.solr.schema.IndexSchema
import org.apache.solr.core._
import sellstome.solr.core.SellstomeSolrCore


object SolrTestHelper {

  /** Initializes a sellstome core container for tests */
  protected class Initializer(coreName: String, dataDirectory: String,
                    solrConfig: SolrConfig, indexSchema: IndexSchema) extends CoreContainer.Initializer {

    def getCoreName: String = coreName

    override def initialize: CoreContainer = {
      val container: sellstome.solr.core.CoreContainer = new sellstome.solr.core.CoreContainer() {
        this.loader          = new SolrResourceLoader( SolrResourceLoader.locateSolrHome())
        this.solrHome        = loader.getInstanceDir()
        this.hostPort        = System.getProperty("hostPort")
        this.hostContext     = "solr"
        this.defaultCoreName = "collection1"
        initZooKeeper(System.getProperty("zkHost"), 10000)
      }
      val dcore: CoreDescriptor = new CoreDescriptor(container, coreName, solrConfig.getResourceLoader.getInstanceDir)
      dcore.setConfigName(solrConfig.getResourceName)
      dcore.setSchemaName(indexSchema.getResourceName)
      val core: SolrCore = new SellstomeSolrCore(coreName, dataDirectory, solrConfig, indexSchema, dcore)
      container.register(coreName, core, false)
      if (container.getZkController == null && core.getUpdateHandler.getUpdateLog != null) {
        core.getUpdateHandler.getUpdateLog.recoverFromLog
      }
      return container
    }
  }
}


/**
 *
 * @author Aliaksandr Zhuhrou
 *
 */
class SolrTestHelper(coreName: String, init: CoreContainer.Initializer)
      extends TestHarness(coreName, init) {

  /**
   * @param dataDirectory path for index data, will not be cleaned up
   * @param solrConfig solrconfig instance
   * @param indexSchema schema instance
   */
  def this(dataDirectory: String, solrConfig: SolrConfig, indexSchema: IndexSchema) =
    this("", new SolrTestHelper.Initializer("", dataDirectory, solrConfig, indexSchema))

  /**
   * @param dataDirectory path for index data, will not be cleaned up
   * @param solrConfig solronfig instance
   * @param schemaFile schema filename
   */
  def this(dataDirectory: String, solrConfig: SolrConfig, schemaFile: String) =
    this(dataDirectory, solrConfig, new IndexSchema(solrConfig, schemaFile, null))

  /**
   * @param dataDirectory path for index data, will not be cleaned up
   * @param configFile solrconfig filename
   * @param schemaFile schema filename
   */
  def this(dataDirectory: String, configFile: String, schemaFile: String) =
    this(dataDirectory, TestHarness.createConfig(configFile), schemaFile)

  /**
   * Assumes "solrconfig.xml" is the config file to use.
   *
   * @param dataDirectory path for index data, will not be cleaned up
   * @param schemaFile path of schema file
   */
  def this(dataDirectory: String, schemaFile: String) =
    this(dataDirectory, "solrconfig.xml", schemaFile)

  /**
   * Assumes "solrconfig.xml" is the config file to use, and
   * "schema.xml" is the schema path to use.
   *
   * @param dataDirectory path for index data, will not be cleaned up
   */
  def this(dataDirectory: String) =
        this(dataDirectory, "schema.xml")


}
