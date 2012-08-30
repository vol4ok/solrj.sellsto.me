package sellstome.solr

import org.apache.solr.util.TestHarness
import org.apache.solr.schema.IndexSchema
import org.apache.solr.core._
import sellstome.solr.core.SellstomeSolrCore
import org.apache.solr.common.util.XML
import java.io.{IOException, StringWriter}


object SolrTestHelper {

  /**Initializes a sellstome core container for tests */
  protected class Initializer(coreName: String, dataDirectory: String,
                              solrConfig: SolrConfig, indexSchema: IndexSchema) extends CoreContainer.Initializer {

    def getCoreName: String = coreName

    override def initialize: CoreContainer = {
      val container: sellstome.solr.core.CoreContainer = new sellstome.solr.core.CoreContainer() {
        this.loader = new SolrResourceLoader(SolrResourceLoader.locateSolrHome())
        this.solrHome = loader.getInstanceDir()
        this.hostPort = System.getProperty("hostPort")
        this.hostContext = "solr"
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
    this(null, new SolrTestHelper.Initializer(null, dataDirectory, solrConfig, indexSchema))

  /**
   * @param dataDirectory path for index data, will not be cleaned up
   * @param solrConfig solronfig instance
   * @param schemaFile schema filename
   */
  def this(dataDirectory: String, solrConfig: SolrConfig, schemaFile: String) =
    this(dataDirectory, solrConfig, new IndexSchema(solrConfig, schemaFile, null))

}
