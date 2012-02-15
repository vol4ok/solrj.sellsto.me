package sellstome.solr.search

import org.apache.solr.search.SolrIndexSearcher
import org.apache.solr.schema.IndexSchema
import org.apache.lucene.index.DirectoryReader
import org.apache.solr.core.{DirectoryFactory, SolrCore}
import org.apache.solr.update.SolrIndexConfig

/**
 * Allows plugin specific logic in solr execution chain
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class SellstomeSolrIndexSearcher(core: SolrCore, schema: IndexSchema, name: String, r: DirectoryReader,
                                 closeReader: Boolean, enableCache: Boolean, reserveDirectory: Boolean, directoryFactory: DirectoryFactory)
      extends SolrIndexSearcher(core, schema, name, r, closeReader, enableCache, reserveDirectory, directoryFactory) {

  def this(core: SolrCore, path: String, schema: IndexSchema,
           config: SolrIndexConfig, name: String,
           enableCache: Boolean, directoryFactory: DirectoryFactory) = this(core, schema, name,
                                                                            core.getIndexReaderFactory().newReader(directoryFactory.get(path, config.lockType)),
                                                                            true, enableCache, false, directoryFactory)

}
