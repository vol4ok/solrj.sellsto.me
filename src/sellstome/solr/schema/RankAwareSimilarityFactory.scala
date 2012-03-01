package sellstome.solr.schema

import org.apache.solr.common.params.SolrParams
import org.apache.solr.schema.{IndexSchema, SchemaAware, SimilarityFactory}
import org.apache.lucene.search.similarities.Similarity
import sellstome.lucene.search.similarities.RankAwareSimilarity

/**
 * Creates a rank aware similarity.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class RankAwareSimilarityFactory extends SimilarityFactory
                                 with    SchemaAware       {

  /**
   * Initializes a similarity factory.
   * @param params params that were provided for a given factory in the schema.xml
   */
  override def init(params: SolrParams) {

  }

  def inform(schema: IndexSchema) {
    Console.println("Yes it works")
  }

  /**
   * Creates a new instance of the rank aware similarity factory.
   * We create a new instance for each invocations of this method.
   * @return a new instance of the rank aware similarity factory
   */
  def getSimilarity: Similarity = new RankAwareSimilarity()

}