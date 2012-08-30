package sellstome.solr.update;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestInfo;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionRangeQuery;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryUtils;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.function.ValueSourceRangeFilter;
import org.apache.solr.update.*;
import org.apache.solr.util.RefCounted;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Most of its code taken from the {@link DirectUpdateHandler2}
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
public class SellstomeUpdateHandler extends UpdateHandler implements SolrCoreState.IndexWriterCloser {

    protected final SolrCoreState solrCoreState;
    protected final Lock commitLock = new ReentrantLock();

    // stats
    AtomicLong addCommands = new AtomicLong();
    AtomicLong addCommandsCumulative = new AtomicLong();
    AtomicLong deleteByIdCommands= new AtomicLong();
    AtomicLong deleteByIdCommandsCumulative= new AtomicLong();
    AtomicLong deleteByQueryCommands= new AtomicLong();
    AtomicLong deleteByQueryCommandsCumulative= new AtomicLong();
    AtomicLong expungeDeleteCommands = new AtomicLong();
    AtomicLong mergeIndexesCommands = new AtomicLong();
    AtomicLong commitCommands= new AtomicLong();
    AtomicLong optimizeCommands= new AtomicLong();
    AtomicLong rollbackCommands= new AtomicLong();
    AtomicLong numDocsPending= new AtomicLong();
    AtomicLong numErrors = new AtomicLong();
    AtomicLong numErrorsCumulative = new AtomicLong();

    // tracks when auto-commit should occur
    protected final CommitTracker commitTracker;
    protected final CommitTracker softCommitTracker;

    public SellstomeUpdateHandler(SolrCore core) throws IOException {
        super(core);

        solrCoreState = new SellstomeSolrCoreState(core.getDirectoryFactory());

        SolrConfig.UpdateHandlerInfo updateHandlerInfo = core.getSolrConfig()
                .getUpdateHandlerInfo();
        int docsUpperBound = updateHandlerInfo.autoCommmitMaxDocs; // getInt("updateHandler/autoCommit/maxDocs", -1);
        int timeUpperBound = updateHandlerInfo.autoCommmitMaxTime; // getInt("updateHandler/autoCommit/maxTime", -1);
        commitTracker = new CommitTracker("Hard", core, docsUpperBound, timeUpperBound, updateHandlerInfo.openSearcher, false);

        int softCommitDocsUpperBound = updateHandlerInfo.autoSoftCommmitMaxDocs; // getInt("updateHandler/autoSoftCommit/maxDocs", -1);
        int softCommitTimeUpperBound = updateHandlerInfo.autoSoftCommmitMaxTime; // getInt("updateHandler/autoSoftCommit/maxTime", -1);
        softCommitTracker = new CommitTracker("Soft", core, softCommitDocsUpperBound, softCommitTimeUpperBound, true, true);
    }

    public SellstomeUpdateHandler(SolrCore core, UpdateHandler updateHandler) throws IOException {
        super(core);
        if (updateHandler instanceof SellstomeUpdateHandler) {
            this.solrCoreState = ((SellstomeUpdateHandler) updateHandler).solrCoreState;
        } else {
            // the impl has changed, so we cannot use the old state - decref it
            updateHandler.decref();
            solrCoreState = new SellstomeSolrCoreState(core.getDirectoryFactory());
        }

        SolrConfig.UpdateHandlerInfo updateHandlerInfo = core.getSolrConfig()
                .getUpdateHandlerInfo();
        int docsUpperBound = updateHandlerInfo.autoCommmitMaxDocs; // getInt("updateHandler/autoCommit/maxDocs", -1);
        int timeUpperBound = updateHandlerInfo.autoCommmitMaxTime; // getInt("updateHandler/autoCommit/maxTime", -1);
        commitTracker = new CommitTracker("Hard", core, docsUpperBound, timeUpperBound, updateHandlerInfo.openSearcher, false);

        int softCommitDocsUpperBound = updateHandlerInfo.autoSoftCommmitMaxDocs; // getInt("updateHandler/autoSoftCommit/maxDocs", -1);
        int softCommitTimeUpperBound = updateHandlerInfo.autoSoftCommmitMaxTime; // getInt("updateHandler/autoSoftCommit/maxTime", -1);
        softCommitTracker = new CommitTracker("Soft", core, softCommitDocsUpperBound, softCommitTimeUpperBound, updateHandlerInfo.openSearcher, true);

        this.ulog = updateHandler.getUpdateLog();
        if (this.ulog != null) {
            this.ulog.init(this, core);
        }
    }

    private void deleteAll() throws IOException {
        SolrCore.log.info(core.getLogId()+"REMOVING ALL DOCUMENTS FROM INDEX");
        RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
        try {
            iw.get().deleteAll();
        } finally {
            iw.decref();
        }
    }

    protected void rollbackWriter() throws IOException {
        numDocsPending.set(0);
        solrCoreState.rollbackIndexWriter(core);

    }

    @Override
    public int addDoc(AddUpdateCommand cmd) throws IOException {
        int rc = -1;
        RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
        try {
            IndexWriter writer = iw.get();
            addCommands.incrementAndGet();
            addCommandsCumulative.incrementAndGet();

            // if there is no ID field, don't overwrite
            if (idField == null) {
                cmd.overwrite = false;
            }

            try {

                if (cmd.overwrite) {

                    // Check for delete by query commands newer (i.e. reordered). This
                    // should always be null on a leader
                    List<UpdateLog.DBQ> deletesAfter = null;
                    if (ulog != null && cmd.getVersion() > 0) {
                        deletesAfter = ulog.getDBQNewer(cmd.getVersion());
                    }

                    if (deletesAfter != null) {
                        log.info("Reordered DBQs detected.  Update=" + cmd + " DBQs="
                                + deletesAfter);
                        List<Query> dbqList = new ArrayList<Query>(deletesAfter.size());
                        for (UpdateLog.DBQ dbq : deletesAfter) {
                            try {
                                DeleteUpdateCommand tmpDel = new DeleteUpdateCommand(cmd.getReq());
                                tmpDel.query = dbq.q;
                                tmpDel.setVersion(-dbq.version);
                                dbqList.add(getQuery(tmpDel));
                            } catch (Exception e) {
                                log.error("Exception parsing reordered query : " + dbq, e);
                            }
                        }

                        addAndDelete(cmd, dbqList);
                    } else {
                        // normal update

                        Term updateTerm;
                        Term idTerm = new Term(idField.getName(), cmd.getIndexedId());
                        boolean del = false;
                        if (cmd.updateTerm == null) {
                            updateTerm = idTerm;
                        } else {
                            del = true;
                            updateTerm = cmd.updateTerm;
                        }

                        Document luceneDocument = cmd.getLuceneDocument();
                        // SolrCore.verbose("updateDocument",updateTerm,luceneDocument,writer);
                        writer.updateDocument(updateTerm, luceneDocument,
                                schema.getAnalyzer());
                        // SolrCore.verbose("updateDocument",updateTerm,"DONE");

                        if (del) { // ensure id remains unique
                            BooleanQuery bq = new BooleanQuery();
                            bq.add(new BooleanClause(new TermQuery(updateTerm),
                                    BooleanClause.Occur.MUST_NOT));
                            bq.add(new BooleanClause(new TermQuery(idTerm), BooleanClause.Occur.MUST));
                            writer.deleteDocuments(bq);
                        }

                        // Add to the transaction log *after* successfully adding to the
                        // index, if there was no error.
                        // This ordering ensures that if we log it, it's definitely been
                        // added to the the index.
                        // This also ensures that if a commit sneaks in-between, that we
                        // know everything in a particular
                        // log version was definitely committed.
                        if (ulog != null) ulog.add(cmd);
                    }

                } else {
                    // allow duplicates
                    writer.addDocument(cmd.getLuceneDocument(), schema.getAnalyzer());
                    if (ulog != null) ulog.add(cmd);
                }

                if ((cmd.getFlags() & UpdateCommand.IGNORE_AUTOCOMMIT) == 0) {
                    commitTracker.addedDocument(-1);
                    softCommitTracker.addedDocument(cmd.commitWithin);
                }

                rc = 1;
            } finally {
                if (rc != 1) {
                    numErrors.incrementAndGet();
                    numErrorsCumulative.incrementAndGet();
                } else {
                    numDocsPending.incrementAndGet();
                }
            }

        } finally {
            iw.decref();
        }

        return rc;
    }

    private void updateDeleteTrackers(DeleteUpdateCommand cmd) {
        if ((cmd.getFlags() & UpdateCommand.IGNORE_AUTOCOMMIT) == 0) {
            softCommitTracker.deletedDocument( cmd.commitWithin );

            if (commitTracker.getTimeUpperBound() > 0) {
                commitTracker.scheduleCommitWithin(commitTracker.getTimeUpperBound());
            }

            if (softCommitTracker.getTimeUpperBound() > 0) {
                softCommitTracker.scheduleCommitWithin(softCommitTracker.getTimeUpperBound());
            }
        }
    }


    // we don't return the number of docs deleted because it's not always possible to quickly know that info.
    @Override
    public void delete(DeleteUpdateCommand cmd) throws IOException {
        deleteByIdCommands.incrementAndGet();
        deleteByIdCommandsCumulative.incrementAndGet();

        Term deleteTerm = new Term(idField.getName(), cmd.getIndexedId());
        // SolrCore.verbose("deleteDocuments",deleteTerm,writer);
        RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
        try {
            iw.get().deleteDocuments(deleteTerm);
        } finally {
            iw.decref();
        }
        // SolrCore.verbose("deleteDocuments",deleteTerm,"DONE");

        if (ulog != null) ulog.delete(cmd);

        updateDeleteTrackers(cmd);
    }

    // we don't return the number of docs deleted because it's not always possible to quickly know that info.
    @Override
    public void deleteByQuery(DeleteUpdateCommand cmd) throws IOException {
        deleteByQueryCommands.incrementAndGet();
        deleteByQueryCommandsCumulative.incrementAndGet();
        boolean madeIt=false;
        try {
            Query q = getQuery(cmd);

            boolean delAll = MatchAllDocsQuery.class == q.getClass();

            // currently for testing purposes.  Do a delete of complete index w/o worrying about versions, don't log, clean up most state in update log, etc
            if (delAll && cmd.getVersion() == -Long.MAX_VALUE) {
                synchronized (this) {
                    deleteAll();
                    ulog.deleteAll();
                    return;
                }
            }

            //
            // synchronized to prevent deleteByQuery from running during the "open new searcher"
            // part of a commit.  DBQ needs to signal that a fresh reader will be needed for
            // a realtime view of the index.  When a new searcher is opened after a DBQ, that
            // flag can be cleared.  If those thing happen concurrently, it's not thread safe.
            //
            synchronized (this) {
                if (delAll) {
                    deleteAll();
                } else {
                    RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
                    try {
                        iw.get().deleteDocuments(q);
                    } finally {
                        iw.decref();
                    }
                }

                if (ulog != null) ulog.deleteByQuery(cmd);
            }

            madeIt = true;

            updateDeleteTrackers(cmd);

        } finally {
            if (!madeIt) {
                numErrors.incrementAndGet();
                numErrorsCumulative.incrementAndGet();
            }
        }
    }

    @Override
    public int mergeIndexes(MergeIndexesCommand cmd) throws IOException {
        mergeIndexesCommands.incrementAndGet();
        int rc;

        log.info("start " + cmd);

        IndexReader[] readers = cmd.readers;
        if (readers != null && readers.length > 0) {
            RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
            try {
                iw.get().addIndexes(readers);
            } finally {
                iw.decref();
            }
            rc = 1;
        } else {
            rc = 0;
        }
        log.info("end_mergeIndexes");

        // TODO: consider soft commit issues
        if (rc == 1 && commitTracker.getTimeUpperBound() > 0) {
            commitTracker.scheduleCommitWithin(commitTracker.getTimeUpperBound());
        } else if (rc == 1 && softCommitTracker.getTimeUpperBound() > 0) {
            softCommitTracker.scheduleCommitWithin(softCommitTracker.getTimeUpperBound());
        }

        return rc;
    }

    public void prepareCommit(CommitUpdateCommand cmd) throws IOException {

        boolean error=true;

        try {
            log.info("start "+cmd);
            RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
            try {
                iw.get().prepareCommit();
            } finally {
                iw.decref();
            }

            log.info("end_prepareCommit");

            error=false;
        }
        finally {
            if (error) numErrors.incrementAndGet();
        }
    }

    @Override
    public void commit(CommitUpdateCommand cmd) throws IOException {
        if (cmd.prepareCommit) {
            prepareCommit(cmd);
            return;
        }

        if (cmd.optimize) {
            optimizeCommands.incrementAndGet();
        } else {
            commitCommands.incrementAndGet();
            if (cmd.expungeDeletes) expungeDeleteCommands.incrementAndGet();
        }

        Future[] waitSearcher = null;
        if (cmd.waitSearcher) {
            waitSearcher = new Future[1];
        }

        boolean error=true;
        try {
            // only allow one hard commit to proceed at once
            if (!cmd.softCommit) {
                commitLock.lock();
            }

            log.info("start "+cmd);

            // We must cancel pending commits *before* we actually execute the commit.

            if (cmd.openSearcher) {
                // we can cancel any pending soft commits if this commit will open a new searcher
                softCommitTracker.cancelPendingCommit();
            }
            if (!cmd.softCommit && (cmd.openSearcher || !commitTracker.getOpenSearcher())) {
                // cancel a pending hard commit if this commit is of equal or greater "strength"...
                // If the autoCommit has openSearcher=true, then this commit must have openSearcher=true
                // to cancel.
                commitTracker.cancelPendingCommit();
            }

            RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
            try {
                IndexWriter writer = iw.get();
                if (cmd.optimize) {
                    writer.forceMerge(cmd.maxOptimizeSegments);
                } else if (cmd.expungeDeletes) {
                    writer.forceMergeDeletes();
                }

                if (!cmd.softCommit) {
                    synchronized (this) { // sync is currently needed to prevent preCommit
                        // from being called between preSoft and
                        // postSoft... see postSoft comments.
                        if (ulog != null) ulog.preCommit(cmd);
                    }

                    // SolrCore.verbose("writer.commit() start writer=",writer);
                    final Map<String,String> commitData = new HashMap<String,String>();
                    commitData.put(SolrIndexWriter.COMMIT_TIME_MSEC_KEY,
                            String.valueOf(System.currentTimeMillis()));
                    writer.commit(commitData);
                    // SolrCore.verbose("writer.commit() end");
                    numDocsPending.set(0);
                    callPostCommitCallbacks();
                } else {
                    callPostSoftCommitCallbacks();
                }
            } finally {
                iw.decref();
            }


            if (cmd.optimize) {
                callPostOptimizeCallbacks();
            }


            if (cmd.softCommit) {
                // ulog.preSoftCommit();
                synchronized (this) {
                    if (ulog != null) ulog.preSoftCommit(cmd);
                    core.getSearcher(true, false, waitSearcher, true);
                    if (ulog != null) ulog.postSoftCommit(cmd);
                }
                // ulog.postSoftCommit();
            } else {
                synchronized (this) {
                    if (ulog != null) ulog.preSoftCommit(cmd);
                    if (cmd.openSearcher) {
                        core.getSearcher(true, false, waitSearcher);
                    } else {
                        // force open a new realtime searcher so realtime-get and versioning code can see the latest
                        RefCounted<SolrIndexSearcher> searchHolder = core.openNewSearcher(true, true);
                        searchHolder.decref();
                    }
                    if (ulog != null) ulog.postSoftCommit(cmd);
                }
                if (ulog != null) ulog.postCommit(cmd); // postCommit currently means new searcher has
                // also been opened
            }

            // reset commit tracking

            if (cmd.softCommit) {
                softCommitTracker.didCommit();
            } else {
                commitTracker.didCommit();
            }

            log.info("end_commit_flush");

            error=false;
        }
        finally {
            if (!cmd.softCommit) {
                commitLock.unlock();
            }

            addCommands.set(0);
            deleteByIdCommands.set(0);
            deleteByQueryCommands.set(0);
            if (error) numErrors.incrementAndGet();
        }

        // if we are supposed to wait for the searcher to be registered, then we should do it
        // outside any synchronized block so that other update operations can proceed.
        if (waitSearcher!=null && waitSearcher[0] != null) {
            try {
                waitSearcher[0].get();
            } catch (InterruptedException e) {
                SolrException.log(log,e);
            } catch (ExecutionException e) {
                SolrException.log(log,e);
            }
        }
    }

    /**
     * Called when the Writer should be opened again - eg when replication replaces
     * all of the index files.
     *
     * @param rollback IndexWriter if true else close
     *
     * @throws IOException
     */
    @Override
    public void newIndexWriter(boolean rollback) throws IOException {
        solrCoreState.newIndexWriter(core, rollback);
    }

    /**
     * @since Solr 1.4
     */
    @Override
    public void rollback(RollbackUpdateCommand cmd) throws IOException {
        rollbackCommands.incrementAndGet();

        boolean error=true;

        try {
            log.info("start "+cmd);

            rollbackWriter();

            //callPostRollbackCallbacks();

            // reset commit tracking
            commitTracker.didRollback();
            softCommitTracker.didRollback();

            log.info("end_rollback");

            error=false;
        }
        finally {
            addCommandsCumulative.set(
                    addCommandsCumulative.get() - addCommands.getAndSet( 0 ) );
            deleteByIdCommandsCumulative.set(
                    deleteByIdCommandsCumulative.get() - deleteByIdCommands.getAndSet( 0 ) );
            deleteByQueryCommandsCumulative.set(
                    deleteByQueryCommandsCumulative.get() - deleteByQueryCommands.getAndSet( 0 ) );
            if (error) numErrors.incrementAndGet();
        }
    }

    @Override
    public UpdateLog getUpdateLog() {
        return ulog;
    }

    @Override
    public void close() throws IOException {
        log.info("closing " + this);

        commitTracker.close();
        softCommitTracker.close();

        numDocsPending.set(0);

        solrCoreState.decref(this);
    }


    public static boolean commitOnClose = true;  // TODO: make this a real config option?

    // IndexWriterCloser interface method - called from solrCoreState.decref(this)
    @Override
    public void closeWriter(IndexWriter writer) throws IOException {
        boolean clearRequestInfo = false;
        commitLock.lock();
        try {
            SolrQueryRequest req = new LocalSolrQueryRequest(core, new ModifiableSolrParams());
            SolrQueryResponse rsp = new SolrQueryResponse();
            if (SolrRequestInfo.getRequestInfo() == null) {
                clearRequestInfo = true;
                SolrRequestInfo.setRequestInfo(new SolrRequestInfo(req, rsp));  // important for debugging
            }


            if (!commitOnClose) {
                if (writer != null) {
                    writer.rollback();
                }

                // we shouldn't close the transaction logs either, but leaving them open
                // means we can't delete them on windows (needed for tests)
                if (ulog != null) ulog.close(false);

                return;
            }

            // do a commit before we quit?
            boolean tryToCommit = writer != null && ulog != null && ulog.hasUncommittedChanges() && ulog.getState() == UpdateLog.State.ACTIVE;

            try {
                if (tryToCommit) {

                    CommitUpdateCommand cmd = new CommitUpdateCommand(req, false);
                    cmd.openSearcher = false;
                    cmd.waitSearcher = false;
                    cmd.softCommit = false;

                    // TODO: keep other commit callbacks from being called?
                    //  this.commit(cmd);        // too many test failures using this method... is it because of callbacks?

                    synchronized (this) {
                        ulog.preCommit(cmd);
                    }

                    // todo: refactor this shared code (or figure out why a real CommitUpdateCommand can't be used)
                    final Map<String,String> commitData = new HashMap<String,String>();
                    commitData.put(SolrIndexWriter.COMMIT_TIME_MSEC_KEY, String.valueOf(System.currentTimeMillis()));
                    writer.commit(commitData);

                    synchronized (this) {
                        ulog.postCommit(cmd);
                    }
                }
            } catch (Throwable th) {
                log.error("Error in final commit", th);
            }

            // we went through the normal process to commit, so we don't have to artificially
            // cap any ulog files.
            try {
                if (ulog != null) ulog.close(false);
            }  catch (Throwable th) {
                log.error("Error closing log files", th);
            }

            if (writer != null) writer.close();

        } finally {
            commitLock.unlock();
            if (clearRequestInfo) SolrRequestInfo.clearRequestInfo();
        }
    }

    /////////////////////////////////////////////////////////////////////
    // SolrInfoMBean stuff: Statistics and Module Info
    /////////////////////////////////////////////////////////////////////

    public String getName() {
        return DirectUpdateHandler2.class.getName();
    }

    public String getVersion() {
        return SolrCore.version;
    }

    public String getDescription() {
        return "Update handler that efficiently directly updates the on-disk main lucene index";
    }

    public Category getCategory() {
        return Category.UPDATEHANDLER;
    }

    public String getSourceId() {
        return "$Id: DirectUpdateHandler2.java 1296712 2012-03-03 22:01:36Z yonik $";
    }

    public String getSource() {
        return "$URL: http://svn.apache.org/repos/asf/lucene/dev/trunk/solr/core/src/java/org/apache/solr/update/DirectUpdateHandler2.java $";
    }

    public URL[] getDocs() {
        return null;
    }

    public NamedList getStatistics() {
        NamedList lst = new SimpleOrderedMap();
        lst.add("commits", commitCommands.get());
        if (commitTracker.getDocsUpperBound() > 0) {
            lst.add("autocommit maxDocs", commitTracker.getDocsUpperBound());
        }
        if (commitTracker.getTimeUpperBound() > 0) {
            lst.add("autocommit maxTime", "" + commitTracker.getTimeUpperBound() + "ms");
        }
        lst.add("autocommits", commitTracker.getCommitCount());
        if (softCommitTracker.getDocsUpperBound() > 0) {
            lst.add("soft autocommit maxDocs", softCommitTracker.getDocsUpperBound());
        }
        if (softCommitTracker.getTimeUpperBound() > 0) {
            lst.add("soft autocommit maxTime", "" + softCommitTracker.getTimeUpperBound() + "ms");
        }
        lst.add("soft autocommits", softCommitTracker.getCommitCount());
        lst.add("optimizes", optimizeCommands.get());
        lst.add("rollbacks", rollbackCommands.get());
        lst.add("expungeDeletes", expungeDeleteCommands.get());
        lst.add("docsPending", numDocsPending.get());
        // pset.size() not synchronized, but it should be fine to access.
        // lst.add("deletesPending", pset.size());
        lst.add("adds", addCommands.get());
        lst.add("deletesById", deleteByIdCommands.get());
        lst.add("deletesByQuery", deleteByQueryCommands.get());
        lst.add("errors", numErrors.get());
        lst.add("cumulative_adds", addCommandsCumulative.get());
        lst.add("cumulative_deletesById", deleteByIdCommandsCumulative.get());
        lst.add("cumulative_deletesByQuery", deleteByQueryCommandsCumulative.get());
        lst.add("cumulative_errors", numErrorsCumulative.get());
        return lst;
    }

    @Override
    public String toString() {
        return "DirectUpdateHandler2" + getStatistics();
    }

    @Override
    public SolrCoreState getSolrCoreState() {
        return solrCoreState;
    }

    @Override
    public void decref() {
        try {
            solrCoreState.decref(this);
        } catch (IOException e) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "", e);
        }
    }

    @Override
    public void incref() {
        solrCoreState.incref();
    }

    private Query getQuery(DeleteUpdateCommand cmd) {
        Query q;
        try {
            // move this higher in the stack?
            QParser parser = QParser.getParser(cmd.getQuery(), "lucene", cmd.getReq());
            q = parser.getQuery();
            q = QueryUtils.makeQueryable(q);

            // Make sure not to delete newer versions
            if (ulog != null && cmd.getVersion() != 0 && cmd.getVersion() != -Long.MAX_VALUE) {
                BooleanQuery bq = new BooleanQuery();
                bq.add(q, BooleanClause.Occur.MUST);
                SchemaField sf = ulog.getVersionInfo().getVersionField();
                ValueSource vs = sf.getType().getValueSource(sf, null);
                ValueSourceRangeFilter filt = new ValueSourceRangeFilter(vs, null, Long.toString(Math.abs(cmd.getVersion())), true, true);
                FunctionRangeQuery range = new FunctionRangeQuery(filt);
                bq.add(range, BooleanClause.Occur.MUST);
                q = bq;
            }

            return q;

        } catch (ParseException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
        }
    }

    /** Add a document execute the deletes as atomically as possible */
    private void addAndDelete(AddUpdateCommand cmd, List<Query> dbqList)
            throws IOException {
        Document luceneDocument = cmd.getLuceneDocument();
        Term idTerm = new Term(idField.getName(), cmd.getIndexedId());

        // see comment in deleteByQuery
        synchronized (this) {
            RefCounted<IndexWriter> iw = solrCoreState.getIndexWriter(core);
            try {
                IndexWriter writer = iw.get();
                writer.updateDocument(idTerm, luceneDocument, core.getSchema()
                        .getAnalyzer());

                for (Query q : dbqList) {
                    writer.deleteDocuments(q);
                }
            } finally {
                iw.decref();
            }

            if (ulog != null) ulog.add(cmd, true);
        }

    }


}