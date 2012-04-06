package sellstome.solr.update

import org.apache.solr.update.DefaultSolrCoreState
import org.apache.solr.core.{SolrCore, DirectoryFactory}

/**
 * This state could be easily shared between solr cores.
 * Also this class is responsible for creating a new [[org.apache.lucene.index.IndexWriter]].
 * So we may use it as the base for plug-in our custom IndexWriter implementation
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class SellstomeSolrCoreState(directoryFactory: DirectoryFactory)
  extends DefaultSolrCoreState(directoryFactory) {

  /** Creates a new [[org.apache.lucene.index.IndexWriter]] instance */
  protected override def createMainIndexWriter(core: SolrCore,
                                               name: String,
                                               removeAllExisting: Boolean,
                                               forceNewDirectory: Boolean) = new SellstomeIndexWriter(name, core.getNewIndexDir(),
                                                                                                      core.getDirectoryFactory(),
                                                                                                      removeAllExisting, core.getSchema(),
                                                                                                      core.getSolrConfig().indexConfig,
                                                                                                      core.getDeletionPolicy(),
                                                                                                      core.getCodec(),
                                                                                                      forceNewDirectory)

}