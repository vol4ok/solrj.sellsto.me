package sellstome.solr.core

import org.apache.solr.schema.IndexSchema
import org.apache.solr.core.{CoreDescriptor, SolrConfig, SolrCore}
import org.apache.solr.search.SolrIndexSearcher
import org.apache.lucene.index.{IndexWriter, DirectoryReader}
import org.apache.solr.common.SolrException
import org.apache.solr.util.RefCounted
import sellstome.solr.search.SellstomeSolrIndexSearcher
import org.apache.lucene.store.Directory
import org.apache.solr.update.{SolrIndexWriter, UpdateHandler}
import collection.mutable.HashSet
import java.io.{IOException, File}
import sellstome.util.Logging
import sellstome.solr.update.SellstomeIndexWriter
import org.slf4j.{LoggerFactory, Logger}


object SellstomeSolrCore {
  /** a logger instance */
  private[core] val logger: Logger = LoggerFactory.getLogger(classOf[SellstomeSolrCore])
  private[core] val dirs = new HashSet[String]
}


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
class SellstomeSolrCore(name: String, dataDir: String, config: SolrConfig, schema: IndexSchema,
                        cd: CoreDescriptor, updateHandler: UpdateHandler, prev: SolrCore)
      extends SolrCore(name, dataDir, config, schema, cd, updateHandler, prev) {

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
      this(name, dataDir, config, schema, cd, null, null)

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

      // if it's not a normal near-realtime update, check that paths haven't changed.
      if (!nrt) {
        indexDirFile = new File(getIndexDir()).getCanonicalFile
        newIndexDirFile = new File(newIndexDir).getCanonicalFile
      }
      searcherLock synchronized {
        newestSearcher = realtimeSearcher
        if (newestSearcher != null) {
          newestSearcher.incref()
        }
      }

      if (newestSearcher != null && solrConfig.reopenReaders
          && (nrt || (indexDirFile.equals(newIndexDirFile)))) {

        var newReader: DirectoryReader = null
        val currentReader: DirectoryReader = newestSearcher.get().getIndexReader()

        if (updateHandlerReopens) {
          val writerRef = getUpdateHandler().getSolrCoreState().getIndexWriter(this)
          try {
            newReader = DirectoryReader.openIfChanged(currentReader, writerRef.get(), true)
          }
          finally {
            writerRef.decref()
          }
        } else {
          newReader = DirectoryReader.openIfChanged(currentReader)
        }

        if (newReader == null) {
          if (realtime) {
            newestSearcher.incref()
            return newestSearcher
          }

          currentReader.incRef()
          newReader = currentReader
        }

        // for now, turn off caches if this is for a realtime reader (caches take a little while to instantiate)
        tmp = new SellstomeSolrIndexSearcher(this, schema, (if (realtime) "realtime" else "main"), newReader, true, !realtime, true, directoryFactory)

      } else {
        // newestSearcher == null at this point

        if (newReaderCreator != null) {
          // this is set in the constructor if there is a currently open index writer
          // so that we pick up any uncommitted changes and so we don't go backwards
          // in time on a core reload
          val newReader = newReaderCreator.call()
          val searcherName = if (realtime) "realtime" else "main"
          tmp = new SolrIndexSearcher(this, schema, searcherName, newReader, true, !realtime, true, directoryFactory)
        } else {
          // normal open that happens at startup
          // verbose("non-reopen START:");
          tmp = new SolrIndexSearcher(this, newIndexDir, schema, getSolrConfig().indexConfig, "main", true, directoryFactory)
          // verbose("non-reopen DONE: searcher=",tmp);
        }
      }

      val searcherList = if (realtime) _realtimeSearchers else _searchers
      val newSearcher: RefCounted[SolrIndexSearcher] = newHolder(tmp, searcherList)

      newSearcher.incref()

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

  /** Initializes a solr index. */
  override def initIndex() {
    import SellstomeSolrCore._
    try {
      val indexDir: String = getNewIndexDir()
      val indexExists: Boolean = getDirectoryFactory.exists(indexDir)
      var firstTime: Boolean = false
      classOf[SolrCore] synchronized {
        firstTime = dirs.add(new File(indexDir).getCanonicalPath)
      }
      var removeLocks: Boolean = solrConfig.unlockOnStartup
      initIndexReaderFactory()
      if (indexExists && firstTime && removeLocks) {
        val dir: Directory = directoryFactory.get(indexDir, getSolrConfig.mainIndexConfig.lockType)
        if (dir != null) {
          if (IndexWriter.isLocked(dir)) {
            logger.warn(logid + "WARNING: Solr index directory '" + indexDir + "' is locked.  Unlocking...")
            IndexWriter.unlock(dir)
          }
          directoryFactory.release(dir)
        }
      }
      if (!indexExists) {
        logger.warn(logid + "Solr index directory '" + new File(indexDir) + "' doesn't exist." + " Creating new index...")
        var writer = new SellstomeIndexWriter("SolrCore.initIndex", indexDir, getDirectoryFactory, true, schema, solrConfig.indexConfig, solrDelPolicy, codec, false)
        writer.close()
      }
    } catch {
      case e: IOException => {
        throw new RuntimeException(e)
      }
    }
  }


}
