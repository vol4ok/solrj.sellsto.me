package sellstome.solr.update

import sellstome.solr.SellstomeSolrTestCaseJ4
import org.apache.solr.core.SolrCore
import org.apache.solr.update.{RollbackUpdateCommand, CommitUpdateCommand, UpdateHandler}
import org.apache.solr.request.{LocalSolrQueryRequest, SolrQueryRequest}
import org.apache.solr.common.params.{MapSolrParams, CommonParams}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.Directory
import java.util.{HashMap, Arrays}
import org.junit.{Test, Before, AfterClass, BeforeClass}

object SellstomeUpdateHandlerComponentTest {

  private var savedFactory: String = null

  @BeforeClass def beforeClass() {
    savedFactory = System.getProperty("solr.DirectoryFactory")
    System.setProperty("solr.directoryFactory", "org.apache.solr.core.MockFSDirectoryFactory")
    SellstomeSolrTestCaseJ4.initCore("solrconfig-sellstome-updatehandler.xml", "schema12.xml")
  }

  @AfterClass def afterClass() {
    if (savedFactory == null) {
      System.clearProperty("solr.directoryFactory")
    } else {
      System.setProperty("solr.directoryFactory", savedFactory)
    }
  }

}

/**
 * Tests the [[sellstome.solr.update.SellstomeUpdateHandler]].
 * The actual code taken from the [[org.apache.solr.update.DirectUpdateHandlerTest]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class SellstomeUpdateHandlerComponentTest extends SellstomeSolrTestCaseJ4 {


  @Before override def setUp() {
    super.setUp()
    clearIndex()
    assertU(commit())
  }

  @Test def testRequireUniqueKey() {
    assertU(adoc("id", "1"))
    assertFailedU(adoc("id", "2", "id", "ignore_exception", "text", "foo"))
    ignoreException("id")
    assertFailedU(adoc("text", "foo"))
    resetExceptionIgnores()
  }

  @Test def testBasics() {
    assertU(adoc("id", "5"))
    assertQ(req("q", "id:5"), "//*[@numFound='0']")
    assertU(commit())
    assertQ(req("q", "id:5"), "//*[@numFound='1']")
    assertU(delI("5"))
    assertQ(req("q", "id:5"), "//*[@numFound='1']")
    assertU(commit())
    assertQ(req("q", "id:5"), "//*[@numFound='0']")
  }

  @Test def testAddRollback() {
    deleteCore()
    initCore("solrconfig-sellstome-updatehandler.xml", "schema12.xml")
    assertU(adoc("id", "A"))
    var core: SolrCore = testHelper.getCore
    var updater: UpdateHandler = core.getUpdateHandler
    assert(updater.isInstanceOf[SellstomeUpdateHandler])
    var updateHandler: SellstomeUpdateHandler = updater.asInstanceOf[SellstomeUpdateHandler]
    var ureq: SolrQueryRequest = req()
    var cmtCmd: CommitUpdateCommand = new CommitUpdateCommand(ureq, false)
    cmtCmd.waitSearcher = true
    assert(1 == updateHandler.addCommands.get)
    assert(1 == updateHandler.addCommandsCumulative.get)
    assert(0 == updateHandler.commitCommands.get)
    updater.commit(cmtCmd)
    assert(0 == updateHandler.addCommands.get)
    assert(1 == updateHandler.addCommandsCumulative.get)
    assert(1 == updateHandler.commitCommands.get)
    ureq.close()
    assertU(adoc("id", "B"))
    ureq = req()
    var rbkCmd: RollbackUpdateCommand = new RollbackUpdateCommand(ureq)
    assert(1 == updateHandler.addCommands.get)
    assert(2 == updateHandler.addCommandsCumulative.get)
    assert(0 == updateHandler.rollbackCommands.get)
    updater.rollback(rbkCmd)
    assert(0 == updateHandler.addCommands.get)
    assert(1 == updateHandler.addCommandsCumulative.get)
    assert(1 == updateHandler.rollbackCommands.get)
    ureq.close()
    var args = new HashMap[String, String]()
    args.put(CommonParams.Q, "id:A OR id:B")
    args.put("indent", "true")
    var request: SolrQueryRequest = new LocalSolrQueryRequest(core, new MapSolrParams(args))
    assertQ("\"B\" should not be found.", request, "//*[@numFound='1']", "//result/doc[1]/str[@name='id'][.='A']")
    assertU(adoc("id", "ZZZ"))
    assertU(commit())
    assertQ("\"ZZZ\" must be found.", req("q", "id:ZZZ"), "//*[@numFound='1']", "//result/doc[1]/str[@name='id'][.='ZZZ']")
  }

  @Test def testDeleteRollback() {
    deleteCore()
    initCore("solrconfig-sellstome-updatehandler.xml", "schema12.xml")
    assertU(adoc("id", "A"))
    assertU(adoc("id", "B"))
    var core: SolrCore = testHelper.getCore()
    var updater: UpdateHandler = core.getUpdateHandler()
    assert(updater.isInstanceOf[SellstomeUpdateHandler])
    var updateHandler: SellstomeUpdateHandler = updater.asInstanceOf[SellstomeUpdateHandler]
    var ureq: SolrQueryRequest = req()
    var cmtCmd: CommitUpdateCommand = new CommitUpdateCommand(ureq, false)
    cmtCmd.waitSearcher = true
    assert(2 == updateHandler.addCommands.get)
    assert(2 == updateHandler.addCommandsCumulative.get)
    assert(0 == updateHandler.commitCommands.get)
    updater.commit(cmtCmd)
    assert(0 == updateHandler.addCommands.get)
    assert(2 == updateHandler.addCommandsCumulative.get)
    assert(1 == updateHandler.commitCommands.get)
    ureq.close()
    var args = new HashMap[String, String]
    args.put(CommonParams.Q, "id:A OR id:B")
    args.put("indent", "true")
    var request: SolrQueryRequest = new LocalSolrQueryRequest(core, new MapSolrParams(args))
    assertQ("\"A\" and \"B\" should be found.", request, "//*[@numFound='2']", "//result/doc[1]/str[@name='id'][.='A']", "//result/doc[2]/str[@name='id'][.='B']")
    assertU(delI("B"))
    assertQ("\"A\" and \"B\" should be found.", request, "//*[@numFound='2']", "//result/doc[1]/str[@name='id'][.='A']", "//result/doc[2]/str[@name='id'][.='B']")
    ureq = req()
    var rbkCmd: RollbackUpdateCommand = new RollbackUpdateCommand(ureq)
    assert(1 == updateHandler.deleteByIdCommands.get)
    assert(1 == updateHandler.deleteByIdCommandsCumulative.get)
    assert(0 == updateHandler.rollbackCommands.get)
    updater.rollback(rbkCmd)
    ureq.close()
    assert(0 == updateHandler.deleteByIdCommands.get)
    assert(0 == updateHandler.deleteByIdCommandsCumulative.get)
    assert(1 == updateHandler.rollbackCommands.get)
    assertQ("\"B\" should be found.", request, "//*[@numFound='2']", "//result/doc[1]/str[@name='id'][.='A']", "//result/doc[2]/str[@name='id'][.='B']")
    assertU(adoc("id", "ZZZ"))
    assertU(commit())
    assertQ("\"ZZZ\" must be found.", req("q", "id:ZZZ"), "//*[@numFound='1']", "//result/doc[1]/str[@name='id'][.='ZZZ']")
  }

  @Test def testExpungeDeletes() {
    assertU(adoc("id", "1"))
    assertU(adoc("id", "2"))
    assertU(commit())
    assertU(adoc("id", "3"))
    assertU(adoc("id", "2"))
    assertU(adoc("id", "4"))
    assertU(commit())
    var sr: SolrQueryRequest = req("q", "foo")
    var r: DirectoryReader = sr.getSearcher.getIndexReader
    assert(r.maxDoc > r.numDocs)
    sr.close()
    assertU(commit("expungeDeletes", "true"))
    sr = req("q", "foo")
    r = sr.getSearcher.getIndexReader()
    assert(r.maxDoc == r.numDocs)
    assert(4 == r.maxDoc)
    sr.close()
  }

  @Test def testPrepareCommit() {
    assertU(adoc("id", "999"))
    assertU(optimize())
    assertU(commit())
    var sr: SolrQueryRequest = req()
    var r: DirectoryReader = sr.getSearcher.getIndexReader
    var d: Directory = r.directory
    info("FILES before addDoc=" + Arrays.asList(d.listAll))
    assertU(adoc("id", "1"))
    var nFiles: Int = d.listAll.length
    info("FILES before prepareCommit=" + Arrays.asList(d.listAll))
    updateJ("", params("prepareCommit", "true"))
    info("FILES after prepareCommit=" + Arrays.asList(d.listAll))
    assert(d.listAll.length > nFiles)
    assertJQ(req("q", "id:1"), "/response/numFound==0")
    updateJ("", params("rollback", "true"))
    assertU(commit())
    assertJQ(req("q", "id:1"), "/response/numFound==0")
    assertU(adoc("id", "1"))
    updateJ("", params("prepareCommit", "true"))
    assertJQ(req("q", "id:1"), "/response/numFound==0")
    assertU(commit())
    assertJQ(req("q", "id:1"), "/response/numFound==1")
    sr.close()
  }

}
