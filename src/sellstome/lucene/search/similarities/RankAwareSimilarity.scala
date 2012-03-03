package sellstome.lucene.search.similarities

import org.apache.lucene.search.similarities.{Similarity, DefaultSimilarity}
import org.apache.lucene.index.AtomicReaderContext
import org.apache.lucene.search.Explanation
import org.apache.lucene.util.BytesRef


/**
 * A scoring implementation that takes into account a external document rank value
 * @param boostField a name of the DocValues field that stores a long rank value
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class RankAwareSimilarity(boostField: String) extends DefaultSimilarity {

  override def exactSimScorer(stats: Similarity.SimWeight, context: AtomicReaderContext): Similarity.ExactSimScorer = {
    val baseScorer  = super.exactSimScorer(stats, context)
    val values      = context.reader.docValues(boostField).getSource()

    return new Similarity.ExactSimScorer {

      /** Calculates a boosted version of score */
      def score(docId: Int, freq: Int): Float = values.getInt(docId).toFloat * baseScorer.score(docId, freq)

      /** Creates explanation for a scoring of a given doc */
      override def explain(docId: Int, freq: Explanation): Explanation = {
        var boostExplanation  = new Explanation(values.getInt(docId), "indexDocValue(" + boostField + ")")
        var simExplanation    = baseScorer.explain(docId, freq)
        var explanation       = new Explanation(boostExplanation.getValue * simExplanation.getValue, "product of:")
        explanation.addDetail(boostExplanation)
        explanation.addDetail(simExplanation)
        return explanation
      }
    }

  }

  override def sloppySimScorer(stats: Similarity.SimWeight, context: AtomicReaderContext): Similarity.SloppySimScorer = {
    val sub      = super.sloppySimScorer(stats, context)
    val values   = context.reader.docValues(boostField).getSource

    return new Similarity.SloppySimScorer {

      def score(docId: Int, freq: Float): Float   = values.getInt(docId).toFloat * sub.score(docId, freq)

      def computeSlopFactor(distance: Int): Float = sub.computeSlopFactor(distance)

      def computePayloadFactor(doc: Int,
                               start: Int,
                               end: Int, payload: BytesRef): Float = sub.computePayloadFactor(doc, start, end, payload)

      override def explain(doc: Int, freq: Explanation): Explanation = {
        var boostExplanation = new Explanation(values.getInt(doc).toFloat, "indexDocValue(" + boostField + ")")
        var simExplanation   = sub.explain(doc, freq)
        var explanation      = new Explanation(boostExplanation.getValue * simExplanation.getValue, "product of:")
        explanation.addDetail(boostExplanation)
        explanation.addDetail(simExplanation)
        return explanation
      }
    }
  }

  override def toString = "RankAwareSimilarity"
}
