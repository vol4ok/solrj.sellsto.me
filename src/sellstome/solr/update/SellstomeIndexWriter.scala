package sellstome.solr.update

import org.apache.solr.core.DirectoryFactory
import org.apache.solr.schema.IndexSchema
import org.apache.solr.update.{SolrIndexConfig, SolrIndexWriter}
import org.apache.lucene.index.IndexDeletionPolicy
import org.apache.lucene.codecs.Codec

/**
 * Extends a functionality of the [[org.apache.solr.update.SolrIndexWriter]].
 * It allows us for example to implement the updatable doc values.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class SellstomeIndexWriter(name: String,
                           path: String,
                           directoryFactory: DirectoryFactory,
                           create: Boolean,
                           schema: IndexSchema,
                           config: SolrIndexConfig,
                           delPolicy: IndexDeletionPolicy,
                           codec: Codec,
                           forceNewDirectory: Boolean) extends SolrIndexWriter(name, path, directoryFactory, create, schema, config, delPolicy, codec, forceNewDirectory) {

}
