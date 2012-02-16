package sellstome.solr.search

import org.apache.solr.schema.IndexSchema
import org.apache.solr.core.{DirectoryFactory, SolrCore}
import org.apache.solr.update.SolrIndexConfig
import org.apache.lucene.search._
import org.apache.solr.search.{DocSlice, QueryUtils, SolrIndexSearcher}
import org.slf4j.LoggerFactory
import SellstomeSolrIndexSearcher._
import org.apache.lucene.index.{AtomicReaderContext, DirectoryReader}
import scala.math._
import sellstome.search.SellstomeTopFieldCollector

object SellstomeSolrIndexSearcher {

  val Log = LoggerFactory.getLogger(classOf[SellstomeSolrIndexSearcher])

}

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

  protected override def getDocListNC(qr: SolrIndexSearcher.QueryResult, cmd: SolrIndexSearcher.QueryCommand): Unit = {
    val timeAllowed: Long = cmd.getTimeAllowed
    val len: Int = cmd.getSupersetMaxDoc
    var last: Int = len
    if (last < 0 || last > maxDoc) last = maxDoc
    val lastDocRequested: Int = last
    var nDocsReturned: Int = 0
    var totalHits: Int = 0
    var maxScore: Float = 0.0f
    var ids: Array[Int] = null
    var scores: Array[Float] = null
    val needScores: Boolean = (cmd.getFlags & SolrIndexSearcher.GET_SCORES) != 0
    val query: Query = QueryUtils.makeQueryable(cmd.getQuery)
    val pf: SolrIndexSearcher.ProcessedFilter = getProcessedFilter(cmd.getFilter, cmd.getFilterList)
    val luceneFilter: Filter = pf.filter
    if (lastDocRequested <= 0) {
      val topscore: Array[Float] = Array[Float](Float.NegativeInfinity)
      val numHits: Array[Int] = new Array[Int](1)
      var collector = if (!needScores) {
                                            new Collector {

                                              def setScorer(scorer: Scorer) {}

                                              def collect(doc: Int) { numHits(0) = numHits(0) + 1 }

                                              def setNextReader(context: AtomicReaderContext) {}

                                              def acceptsDocsOutOfOrder = true

                                            }
      } else {
                                            new Collector {

                                              private[search] var scorer: Scorer = null

                                              def setScorer(scorer: Scorer) {
                                                this.scorer = scorer
                                              }

                                              def collect(doc: Int) {
                                                numHits(0) = numHits(0) + 1;
                                                val score = scorer.score
                                                if (score > topscore(0)) topscore(0) = score
                                              }

                                              def setNextReader(context: AtomicReaderContext) {}

                                              def acceptsDocsOutOfOrder = true

                                            }
      }

      if (timeAllowed > 0) {
        collector = new TimeLimitingCollector(collector, TimeLimitingCollector.getGlobalCounter, timeAllowed)
      }
      if (pf.postFilter != null) {
        pf.postFilter.setLastDelegate(collector)
        collector = pf.postFilter
      }
      try {
        super.search(query, luceneFilter, collector)
      }
      catch {
        case x: TimeLimitingCollector.TimeExceededException => {
          Log.warn("Query: " + query + "; " + x.getMessage)
          qr.setPartialResults(true)
        }
      }
      nDocsReturned = 0
      ids = new Array[Int](nDocsReturned)
      scores = new Array[Float](nDocsReturned)
      totalHits = numHits(0)
      maxScore = if (totalHits > 0) topscore(0) else 0.0f
    } else {
      var topCollector: TopDocsCollector[_ <: ScoreDoc] = null
      if (cmd.getSort == null) {
        if (cmd.getScoreDoc != null) {
          topCollector = TopScoreDocCollector.create(len, cmd.getScoreDoc, true)
        }
        else {
          topCollector = TopScoreDocCollector.create(len, true)
        }
      }
      else {
        topCollector = SellstomeTopFieldCollector.create(weightSort(cmd.getSort), len, readerContext, false, needScores, needScores, true)
      }
      var collector: Collector = topCollector
      if (timeAllowed > 0) {
        collector = new TimeLimitingCollector(collector, TimeLimitingCollector.getGlobalCounter, timeAllowed)
      }
      if (pf.postFilter != null) {
        pf.postFilter.setLastDelegate(collector)
        collector = pf.postFilter
      }
      try {
        super.search(query, luceneFilter, collector)
      }
      catch {
        case x: TimeLimitingCollector.TimeExceededException => {
          Log.warn("Query: " + query + "; " + x.getMessage)
          qr.setPartialResults(true)
        }
      }
      totalHits = topCollector.getTotalHits
      val topDocs: TopDocs = topCollector.topDocs(0, len)
      maxScore = if (totalHits > 0) topDocs.getMaxScore else 0.0f
      nDocsReturned = topDocs.scoreDocs.length
      ids = new Array[Int](nDocsReturned)
      scores = if ((cmd.getFlags & SolrIndexSearcher.GET_SCORES) != 0) new Array[Float](nDocsReturned) else null

      var i: Int = 0
      while (i < nDocsReturned) {
        val scoreDoc: ScoreDoc = topDocs.scoreDocs(i)
        ids(i) = scoreDoc.doc
        if (scores != null) scores(i) = scoreDoc.score
        i += 1
      }

    }
    var sliceLen: Int = min(lastDocRequested, nDocsReturned)
    if (sliceLen < 0) sliceLen = 0
    qr.setDocList(new DocSlice(0, sliceLen, ids, scores, totalHits, maxScore))
  }



}
