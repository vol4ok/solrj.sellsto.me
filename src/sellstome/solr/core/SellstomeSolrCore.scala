package sellstome.solr.core

import org.apache.solr.schema.IndexSchema
import org.apache.solr.core.{CoreDescriptor, SolrConfig, SolrCore}
import org.apache.solr.update.UpdateHandler
import org.apache.solr.search.SolrIndexSearcher
import java.io.File
import org.apache.lucene.index.{IndexWriter, DirectoryReader}
import org.apache.solr.common.SolrException
import org.apache.solr.util.RefCounted
import sellstome.solr.search.SellstomeSolrIndexSearcher


/**
 * Allows plug-in customized solr classes. For example our custom implementation of
 * SolrSearchIndexer
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param dataDir the index directory
 * @param config a solr config instance
 * @param schema a solr schema instance
 * @param updateHandler
 */
class SellstomeSolrCore(name: String, dataDir: String, config: SolrConfig, schema: IndexSchema, cd: CoreDescriptor, updateHandler: UpdateHandler)
      extends SolrCore(name, dataDir, config, schema, cd, updateHandler) {

  /**
   * Creates a new core and register it in the list of cores.
   * If a core with the same name already exists, it will be stopped and replaced by this one.
   *
   * @param name
   * @param dataDir the index directory
   * @param config a solr config instance
   * @param schema a solr schema instance
   *
   * @since solr 1.3
   */
  def this(name: String, dataDir: String, config: SolrConfig, schema: IndexSchema, cd: CoreDescriptor) =
      this(name, dataDir, config, schema, cd, null)

  /**
   * Opens a new searcher and returns a RefCounted<SolrIndexSearcher> with it's reference incremented.
   *
   * "realtime" means that we need to open quickly for a realtime view of the index, hence don't do any
   * autowarming and add to the _realtimeSearchers queue rather than the _searchers queue (so it won't
   * be used for autowarming by a future normal searcher).  A "realtime" searcher will currently never
   * become "registered" (since it currently lacks caching).
   *
   * realtimeSearcher is updated to the latest opened searcher, regardless of the value of "realtime".
   *
   * This method aquires openSearcherLock - do not call with searckLock held!
   */
  override def openNewSearcher(updateHandlerReopens: Boolean, realtime: Boolean): RefCounted[SolrIndexSearcher] = {
    var tmp: SolrIndexSearcher = null
    var newestSearcher: RefCounted[SolrIndexSearcher] = null
    val nrt: Boolean = solrConfig.reopenReaders && updateHandlerReopens
    openSearcherLock.lock()
    try {
      val newIndexDir: String = getNewIndexDir()
      var indexDirFile: File = null
      var newIndexDirFile: File = null
      if (!nrt) {
        indexDirFile = new File(getIndexDir).getCanonicalFile
        newIndexDirFile = new File(newIndexDir).getCanonicalFile
      }
      searcherLock synchronized {
        newestSearcher = realtimeSearcher
        if (newestSearcher != null) {
          newestSearcher.incref
        }
      }
      if (newestSearcher != null && solrConfig.reopenReaders && (nrt || (indexDirFile == newIndexDirFile))) {
        var newReader: DirectoryReader = null
        val currentReader: DirectoryReader = newestSearcher.get.getIndexReader
        if (updateHandlerReopens) {
          val writer: IndexWriter = getUpdateHandler.getSolrCoreState.getIndexWriter(this)
          newReader = DirectoryReader.openIfChanged(currentReader, writer, true)
        }
        else {
          newReader = DirectoryReader.openIfChanged(currentReader)
        }
        if (newReader == null) {
          if (realtime) {
            newestSearcher.incref
            return newestSearcher
          }
          currentReader.incRef()
          newReader = currentReader
        }
        tmp = new SellstomeSolrIndexSearcher(this, schema, (if (realtime) "realtime" else "main"), newReader, true, !realtime, true, directoryFactory)
      }
      else {
        tmp = new SellstomeSolrIndexSearcher(this, newIndexDir, schema, getSolrConfig.mainIndexConfig, "main", true, directoryFactory)
      }
      val searcherList = if (realtime) _realtimeSearchers else _searchers
      val newSearcher: RefCounted[SolrIndexSearcher] = newHolder(tmp, searcherList)
      newSearcher.incref
      searcherLock synchronized {
        if (realtimeSearcher != null) {
          realtimeSearcher.decref()
        }
        realtimeSearcher = newSearcher
        searcherList.add(realtimeSearcher)
      }
      return newSearcher
    }
    catch {
      case e: Exception => {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Error opening new searcher", e)
      }
    }
    finally {
      openSearcherLock.unlock()
      if (newestSearcher != null) {
        newestSearcher.decref()
      }
    }
  }

  /**
   * Gets a non-caching searcher.
   * Even if we don't use this method it is better to override it.
   */
  override def newSearcher(name: String): SolrIndexSearcher = {
    return new SellstomeSolrIndexSearcher(this, getNewIndexDir, schema, getSolrConfig.mainIndexConfig, name, false, directoryFactory)
  }


}
