package sellstome.search.solr.response

import org.easymock.EasyMock
import org.apache.solr.search.{SolrIndexSearcher, DocIterator, DocList}
import org.apache.lucene.document.Document
import org.powermock.api.easymock.PowerMock
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.powermock.modules.agent.PowerMockAgent

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 13.10.11
 * Time: 0:35
 * Test json ad search result serializer.
 *
 * todo zhugrov a - fix underlying test
 */
class AdSerializerUnitTest extends FunSuite with BeforeAndAfterAll {

  /** Initialize a power mock agent */
  protected override def beforeAll() {
    PowerMockAgent.initializeIfNeeded()
  }

  test("check json generated") {
    val docList = EasyMock.createMock(classOf[DocList])
    EasyMock.expect(docList.iterator()).andReturn(new MockDocIterator(List(0))).anyTimes()
    EasyMock.expect(docList.size()).andReturn(1).anyTimes()
    EasyMock.expect(docList.offset()).andReturn(0).anyTimes()
    val searcher = EasyMock.createMock(classOf[SolrIndexSearcher])
    val doc = PowerMock.createMock(classOf[Document])
    EasyMock.expect(doc.get(EasyMock.anyObject[String]())).andReturn("test").anyTimes()
    EasyMock.expect(searcher.doc(0)).andReturn(doc).anyTimes()
    EasyMock.replay(docList, searcher, doc)
    val json = AdsSerializer( docList, searcher)
    assert( json != null )
  }

}

/** Mock object for the {@link DocIterator} */
private class MockDocIterator(docIds: List[Int]) extends DocIterator {

  val internalIt: Iterator[Int] = docIds.iterator

  def nextDoc() = internalIt.next()

  def score() = 0.0f

  def hasNext = internalIt.hasNext

  def next() = internalIt.next()

  def remove() {
    throw new UnsupportedOperationException("Operation is not supported for mock object")
  }

}