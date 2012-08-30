package sellstome.solr.core

import org.xml.sax.{SAXException, InputSource}
import java.io.{IOException, ByteArrayInputStream, File}
import org.slf4j.{LoggerFactory, Logger}
import org.apache.solr.common.cloud.ZooKeeperException
import org.apache.solr.common.SolrException
import org.apache.solr.cloud.ZkSolrResourceLoader
import org.apache.solr.schema.IndexSchema
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import org.apache.solr.core.{CoreDescriptor, SolrConfig, SolrCore, SolrResourceLoader}
import CoreContainer._
import org.apache.zookeeper.KeeperException
import javax.xml.parsers.ParserConfigurationException
import org.apache.solr.core.{CoreContainer => BaseCoreContainer}


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
      val solrHome: String = SolrResourceLoader.locateSolrHome
      val fconf: File = new File(solrHome, if (containerConfigFilename == null) "solr.xml" else containerConfigFilename)
      Log.info("looking for solr.xml: " + fconf.getAbsolutePath)
      val cores: CoreContainer = new CoreContainer(solrHome)

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
class CoreContainer(solrHome: String) extends BaseCoreContainer(solrHome) {

  def this() = this(SolrResourceLoader.locateSolrHome())

  /**
   * Creates a new core based on a descriptor but does not register it.
   * We override a base method as it allows us instantiate our custom solr core.
   *
   * @param  dcore a core descriptor
   * @return the newly created core
   */
  override def create(dcore: CoreDescriptor): SolrCore = {

    //:TODO: would be really nice if this method wrapped any underlying errors and only threw SolrException
    val name: String = dcore.getName

    var failure: Exception = null
    try {
      var idir: File = new File(dcore.getInstanceDir)
      if (!idir.isAbsolute) {
        idir = new File(solrHome, dcore.getInstanceDir)
      }
      val instanceDir: String = idir.getPath
      Log.info("Creating SolrCore '{}' using instanceDir: {}", dcore.getName, instanceDir)
      var solrLoader: SolrResourceLoader = null
      var config: SolrConfig = null
      var zkConfigName: String = null
      if (zkController == null) {
        solrLoader = new SolrResourceLoader(instanceDir,
                                            libLoader,
                                            BaseCoreContainer.getCoreProps(instanceDir, dcore.getPropertiesName, dcore.getCoreProperties))
        config = new SolrConfig(solrLoader, dcore.getConfigName, null)
      }
      else {
        try {
          val collection: String = dcore.getCloudDescriptor.getCollectionName
          zkController.createCollectionZkNode(dcore.getCloudDescriptor)
          zkConfigName = zkController.readConfigName(collection)
          if (zkConfigName == null) {
            Log.error("Could not find config name for collection:" + collection)
            throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "Could not find config name for collection:" + collection)
          }
          solrLoader = new ZkSolrResourceLoader(instanceDir,
                                                zkConfigName,
                                                libLoader,
                                                BaseCoreContainer.getCoreProps(instanceDir, dcore.getPropertiesName, dcore.getCoreProperties),
                                                zkController)
          config = getSolrConfigFromZk(zkConfigName, dcore.getConfigName, solrLoader)
        }
        catch {
          case e: KeeperException => {
            Log.error("", e)
            throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e)
          }
          case e: InterruptedException => {
            Thread.currentThread.interrupt()
            Log.error("", e)
            throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e)
          }
        }
      }

      var schema: IndexSchema = null
      if (indexSchemaCache != null) {
        if (zkController != null) {
          var schemaFile: File = new File(dcore.getSchemaName)
          if (!schemaFile.isAbsolute) {
            schemaFile = new File(solrLoader.getInstanceDir + "conf" + File.separator + dcore.getSchemaName)
          }
          if (schemaFile.exists) {
            val key: String = schemaFile.getAbsolutePath + ":" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.ROOT).format(new Date(schemaFile.lastModified))
            schema = indexSchemaCache.get(key)
            if (schema == null) {
              Log.info("creating new schema object for core: " + dcore.getName())
              schema = new IndexSchema(config, dcore.getSchemaName, null)
              indexSchemaCache.put(key, schema)
            }
            else {
              Log.info("re-using schema object for core: " + dcore.getName())
            }
          }
        }
      }

      if (schema == null) {
        if (zkController != null) {
          try {
            schema = getSchemaFromZk(zkConfigName, dcore.getSchemaName, config, solrLoader)
          }
          catch {
            case e: KeeperException => {
              Log.error("", e)
              throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e)
            }
            case e: InterruptedException => {
              Thread.currentThread.interrupt()
              Log.error("", e)
              throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e)
            }
          }
        }
        else {
          schema = new IndexSchema(config, dcore.getSchemaName, null)
        }
      }

      val core: SolrCore = new SellstomeSolrCore(dcore.getName, null, config, schema, dcore)
      if (zkController == null && core.getUpdateHandler.getUpdateLog != null) {
        core.getUpdateHandler.getUpdateLog.recoverFromLog
      }
      return core
    }
    catch {
      case parserConfError: ParserConfigurationException => {
        failure = parserConfError
        throw parserConfError
      }
      case ioError: IOException => {
        failure = ioError
        throw ioError
      }
      case parseError: SAXException => {
        failure = parseError
        throw parseError
      }
      case otherError: RuntimeException => {
        failure = otherError
        throw otherError
      }
    }
    finally {
      coreInitFailures synchronized {
        coreInitFailures.remove(name)
        if (null != failure) {
          coreInitFailures.put(name, failure)
        }
      }
    }
  }

}
