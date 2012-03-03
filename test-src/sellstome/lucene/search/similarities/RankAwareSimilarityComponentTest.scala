package sellstome.lucene.search.similarities

import sellstome.lucene.util.SellstomeLuceneTestCase
import org.apache.lucene.store.Directory
import org.apache.lucene.analysis.MockAnalyzer
import org.apache.lucene.search.similarities.DefaultSimilarity
import org.apache.lucene.document.{TextField, Document}
import org.apache.lucene.index._
import org.apache.lucene.search._
import java.util.ArrayList

/**
 * tests the functionality for the rank aware similarity.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class RankAwareSimilarityComponentTest extends SellstomeLuceneTestCase {

  protected val SearchField: String             = "message"
  protected val RankField: String               = "rank"
  protected var values: Array[(String, Long)]   = Array(("all",       1l),
                                                        ("dogs dogs", 1l),
                                                        ("like",      1l),
                                                        ("playing",   1l),
                                                        ("fetch",     1l),
                                                        ("all",       2l))
  protected var directory: Directory            = null
  protected var indexSearcher: IndexSearcher    = null
  protected var indexReader: IndexReader        = null

  override def setUp() {
    super.setUp()
    directory = newDirectory()
    val writer: RandomIndexWriter =
      new RandomIndexWriter(random, directory, newIndexWriterConfig(TestVersionCurrent, new MockAnalyzer(random))
                                               .setMergePolicy(newLogMergePolicy())
                                               .setSimilarity(new RankAwareSimilarity(RankField)))
    var i: Int = 0
    while (i < values.length) {
      val (message, rank) = values(i)
      val doc: Document = new Document
      doc.add(newField(SearchField, message, TextField.TYPE_STORED))
      doc.add(newDocValueField(RankField, rank))
      writer.addDocument(doc)
      i = i + 1
    }
    indexReader = SlowCompositeReaderWrapper.wrap(writer.getReader)
    writer.close()
    indexSearcher = newSearcher(indexReader)
    indexSearcher.setSimilarity(new RankAwareSimilarity(RankField))
  }

  override def tearDown() {
    indexReader.close()
    directory.close()
    super.tearDown()
  }

  def test() {
    val allTerm: Term = new Term(SearchField, "all")
    val termQuery: TermQuery = new TermQuery(allTerm)
    val weight: Weight = indexSearcher.createNormalizedWeight(termQuery)
    assertTrue(indexSearcher.getTopReaderContext.isInstanceOf[AtomicReaderContext])
    val context: AtomicReaderContext = indexSearcher.getTopReaderContext.asInstanceOf[AtomicReaderContext]
    val ts: Scorer = weight.scorer(context, true, true, context.reader.getLiveDocs)
    val docs: java.util.List[TestHit] = new ArrayList[TestHit]()
    ts.score(new Collector {

      def setScorer(scorer: Scorer) {
        this.scorer = scorer
      }

      def collect(doc: Int) {
        val score: Float = scorer.score
        val docId = doc + base
        docs.add(new TestHit(docId, score))
        assertTrue("score " + score + " is not greater than 0", score > 0)
        assertTrue("Doc: " + docId + " does not equal 0 or doc does not equal 5", docId == 0 || docId == 5)
      }

      def setNextReader(context: AtomicReaderContext) {
        base = context.docBase
      }

      def acceptsDocsOutOfOrder: Boolean = true

      private var base: Int = 0
      private var scorer: Scorer = null
    })
    assertTrue("docs Size: " + docs.size + " is not: " + 2, docs.size == 2)
    val doc0  = docs.get(0)
    val doc5  = docs.get(1)
    assertFalse(doc0.score + " does equal: " + doc5.score, doc0.score == doc5.score)
    /*
     * Score should be (based on Default Sim.: All floats are approximate tf = 1
     * numDocs = 6 docFreq(all) = 2 idf = ln(6/3) + 1 = 1.693147 idf ^ 2 =
     * 2.8667 boost = 1 lengthNorm = 1 //there is 1 term in every document coord
     * = 1 sumOfSquaredWeights = (idf * boost) ^ 2 = 1.693147 ^ 2 = 2.8667
     * queryNorm = 1 / (sumOfSquaredWeights)^0.5 = 1 /(1.693147) = 0.590
     *
     * score = 1 * 2.8667 * 1 * 1 * 0.590 = 1.69
     * Note that doc0 has rank equal to 1
     *           doc5 has rank equal to 2
     * So the final score for the doc5 should be 2 times larger
     */
    assertTrue(doc0.score + " does not equal: " + 1.6931472f, doc0.score == 1.6931472f)
    assertTrue(doc5.score + " does not equal: " + 1.6931472f * 2.0f, doc5.score == (1.6931472f * 2.0f))
  }

  def testNext() {
    var allTerm: Term = new Term(SearchField, "all")
    var termQuery: TermQuery = new TermQuery(allTerm)
    var weight: Weight = indexSearcher.createNormalizedWeight(termQuery)
    assertTrue(indexSearcher.getTopReaderContext.isInstanceOf[AtomicReaderContext])
    var context: AtomicReaderContext = indexSearcher.getTopReaderContext.asInstanceOf[AtomicReaderContext]
    var ts: Scorer = weight.scorer(context, true, true, context.reader.getLiveDocs)
    assertTrue("next did not return a doc", ts.nextDoc != DocIdSetIterator.NO_MORE_DOCS)
    assertTrue("score is not correct", ts.score == 1.6931472f)
    assertTrue("next did not return a doc", ts.nextDoc != DocIdSetIterator.NO_MORE_DOCS)
    assertTrue("score is not correct", ts.score == (1.6931472f * 2.0f))
    assertTrue("next returned a doc and it should not have", ts.nextDoc == DocIdSetIterator.NO_MORE_DOCS)
  }

  def testAdvance() {
    var allTerm: Term = new Term(SearchField, "all")
    var termQuery: TermQuery = new TermQuery(allTerm)
    var weight: Weight = indexSearcher.createNormalizedWeight(termQuery)
    assertTrue(indexSearcher.getTopReaderContext.isInstanceOf[AtomicReaderContext])
    var context: AtomicReaderContext = indexSearcher.getTopReaderContext.asInstanceOf[AtomicReaderContext]
    var ts: Scorer = weight.scorer(context, true, true, context.reader.getLiveDocs)
    assertTrue("Didn't skip", ts.advance(3) != DocIdSetIterator.NO_MORE_DOCS)
    assertTrue("doc should be number 5", ts.docID == 5)
  }

  protected class TestHit(docId: Int, scoreValue: Float) {

    override def toString: String = {
      return "TestHit{" + "doc=" + docId + ", score=" + scoreValue + "}"
    }

    def doc = docId

    def score = scoreValue

  }

}