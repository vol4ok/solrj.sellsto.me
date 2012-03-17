package sellstome.solr.response.json

import org.easymock.EasyMock
import org.apache.solr.search.{SolrIndexSearcher, DocIterator, DocList}
import org.apache.lucene.document.Document
import org.powermock.api.easymock.PowerMock
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.powermock.modules.agent.PowerMockAgent
import java.io.StringWriter
import org.apache.commons.lang.StringUtils
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.common.params.SolrParams
import org.apache.solr.response.{ResultContext, SolrQueryResponse}
import org.apache.solr.common.util.SimpleOrderedMap
import org.apache.solr.schema.{StrField, SchemaField, IndexSchema}
import com.google.common.collect.Maps
import org.apache.lucene.index.IndexableField
import sellstome.solr.common.NotImplementedException
import sellstome.BaseUnitTest

/**
 * Tests a sellstome json search result serializer.
 * @author Zhuhrou Aliaksandr
 * @since 1.0
 */
class JSONSerializerUnitTest extends BaseUnitTest with BeforeAndAfterAll {

  /** Initialize a power mock agent */
  protected override def beforeAll() {
    PowerMockAgent.initializeIfNeeded()
  }

  test("check json generated") {
    val writer = new StringWriter()
    val req    = prepareMockSolrQueryRequest()
    val rsp    = prepareMockSolrQueryResponse()
    JSONSerializer.writeResponse(writer, req, rsp)
    assert( !StringUtils.isEmpty(writer.toString()) )
  }
  
  private def prepareMockSolrQueryRequest(): SolrQueryRequest = {

    val schema = prepareIndexSchema()

    val searcher = prepareSolrIndexSearcher()
    
    val req = new SolrQueryRequest {

      def getStartTime = 0L

      def getSchema = schema

      def getSearcher = searcher

      def setParams(params: SolrParams) {}

      def getParamString = ""

      def getContentStreams = throw new NotImplementedException()

      def getContext = throw new NotImplementedException()

      def getParams = throw new NotImplementedException()

      def close() {}

      def getCore = throw new NotImplementedException()

      def getOriginalParams = throw new NotImplementedException()

    }

    return req
  }
  
  private def prepareIndexSchema(): IndexSchema = {
    val schema = PowerMock.createMock(classOf[IndexSchema])
    val testSchemaField = PowerMock.createMock(classOf[SchemaField])
    EasyMock.expect(testSchemaField.stored()).andReturn(true).anyTimes()
    EasyMock.expect(testSchemaField.getName).andReturn("test").anyTimes()
    EasyMock.expect(testSchemaField.getType).andReturn(new StrField()).anyTimes()
    val schemaFields = Maps.newHashMap[String,SchemaField]()
    schemaFields.put("test", testSchemaField)
    EasyMock.expect(schema.getFields).andReturn(schemaFields).anyTimes()
    EasyMock.replay(schema, testSchemaField)
    return schema
  }
  
  private def prepareSolrIndexSearcher(): SolrIndexSearcher = {
    val indexableField = EasyMock.createMock(classOf[IndexableField])
    EasyMock.expect(indexableField.stringValue()).andReturn("test").anyTimes()
    val searcher = EasyMock.createMock(classOf[SolrIndexSearcher])
    val doc = PowerMock.createMock(classOf[Document])
    EasyMock.expect(doc.getField("test")).andReturn(indexableField).anyTimes()
    EasyMock.expect(searcher.doc(0)).andReturn(doc).anyTimes()
    EasyMock.replay(indexableField, searcher, doc)
    return searcher
  }
  
  private def prepareMockSolrQueryResponse(): SolrQueryResponse = {
    val rsp = new SolrQueryResponse()
    val docList = EasyMock.createMock(classOf[DocList])
    EasyMock.expect(docList.iterator()).andReturn(new MockDocIterator(List(0))).anyTimes()
    EasyMock.expect(docList.size()).andReturn(1).anyTimes()
    EasyMock.expect(docList.offset()).andReturn(0).anyTimes()
    EasyMock.replay(docList)
    val context = new ResultContext()
    context.docs = docList
    val rspValues = new SimpleOrderedMap[AnyRef]()
    rspValues.add("response", context)
    rsp.setAllValues(rspValues)
    return rsp
  }
  

}

/** Mock object for the {@link DocIterator}*/
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