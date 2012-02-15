package sellstome.solr.core

import org.xml.sax.InputSource
import java.io.{ByteArrayInputStream, File}
import org.slf4j.{LoggerFactory, Logger}
import org.apache.solr.common.cloud.ZooKeeperException
import org.apache.solr.common.SolrException
import org.apache.solr.cloud.ZkSolrResourceLoader
import org.apache.solr.schema.IndexSchema
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import org.apache.solr.core.{CoreDescriptor, SolrConfig, SolrCore, SolrResourceLoader}
import CoreContainer._


object CoreContainer {

  val Log: Logger = LoggerFactory.getLogger(classOf[CoreContainer])

  val DefaultCoreName =     "collection1"
  val DefSolrXML =          "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
    "<solr persistent=\"false\">\n" +
    "  <cores adminPath=\"/admin/cores\" defaultCoreName=\"" + DefaultCoreName + "\">\n" +
    "    <core name=\"" + DefaultCoreName + "\" shard=\"${shard:}\" instanceDir=\".\" />\n" +
    "  </cores>\n" +
    "</solr>"

  /** Initializes a new core container. */
  class Initializer extends org.apache.solr.core.CoreContainer.Initializer {

    override def initialize: CoreContainer = {
      val cores: CoreContainer = new CoreContainer()
      val solrHome: String = SolrResourceLoader.locateSolrHome
      val fconf: File = new File(solrHome, if (containerConfigFilename == null) "solr.xml" else containerConfigFilename)
      Log.info("looking for solr.xml: " + fconf.getAbsolutePath)
      if (fconf.exists) {
        cores.load(solrHome, fconf)
      } else {
        Log.info("no solr.xml file found - using default")
        cores.load(solrHome, new InputSource(new ByteArrayInputStream(DefSolrXML.getBytes("UTF-8"))))
        cores.configFile = fconf
      }
      containerConfigFilename = cores.getConfigFile.getName
      return cores
    }
  }


}

/**
 * This subclass allows us to create our customized SolrIndexSearcher
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class CoreContainer extends org.apache.solr.core.CoreContainer {

  /**
   * Creates a new core based on a descriptor but does not register it.
   * We override a base method as it allows us instantiate our custom solr core.
   *
   * @param  dcore a core descriptor
   * @return the newly created core
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws java.io.IOException
   * @throws org.xml.sax.SAXException
   */
  override def create(dcore: CoreDescriptor): SolrCore = {
    var idir: File = new File(dcore.getInstanceDir)
    if (!idir.isAbsolute) { idir = new File(solrHome, dcore.getInstanceDir) }
    val instanceDir: String = idir.getPath
    val config = new SolrConfig( instanceDir, dcore.getConfigName, null)
    val schema = new IndexSchema(config, dcore.getSchemaName, null)
    val core = new SellstomeSolrCore(dcore.getName, null, config, schema, dcore)
    if (core.getUpdateHandler.getUpdateLog != null) {
      core.getUpdateHandler.getUpdateLog.recoverFromLog
    }
    return core
  }

}
