package sellstome.solr.schema

import org.apache.lucene.search.similarities.Similarity
import sellstome.lucene.search.similarities.RankAwareSimilarity
import com.google.common.collect.{Iterables, Maps}
import com.google.common.base.Predicate
import java.util.Map.Entry
import org.apache.solr.schema.{SchemaField, IndexSchema, SchemaAware, SimilarityFactory}

/**
 * Creates a rank aware similarity.
 * You should define exactly one required field with type of sellstome.solr.schema.DocRankFieldType in schema.xml
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class RankAwareSimilarityFactory extends SimilarityFactory
                                 with SchemaAware {

  /** the name of field that contains a static boost signal */
  var boostField: Option[String] = None

  def inform(schema: IndexSchema) {
    val rankField = Iterables.find( schema.getFields().values() , new Predicate[SchemaField] {
      def apply(schemaField: SchemaField) = schemaField.getType().isInstanceOf[DocRankFieldType]
    })
    boostField = Some(rankField.getName())
  }

  /**
   * Creates a new instance of the rank aware similarity factory.
   * We create a new instance for each invocations of this method.
   * @return a new instance of the rank aware similarity factory
   */
  def getSimilarity: Similarity = new RankAwareSimilarity(boostField.get)

}