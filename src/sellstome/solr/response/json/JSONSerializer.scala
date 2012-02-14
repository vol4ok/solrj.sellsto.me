package sellstome.solr.response.json

import converters.FieldConverters
import org.apache.lucene.document.Document
import javax.annotation.Nonnull
import scala.collection.JavaConversions._
import org.json.{JSONObject, JSONArray}
import java.util.Map
import org.apache.solr.search.DocList
import org.apache.solr.common.util.NamedList
import java.io.Writer
import org.apache.solr.request.SolrQueryRequest
import scala.Predef._
import org.apache.solr.response.{ResultContext, SolrQueryResponse}
import com.google.common.collect.Iterables
import com.google.common.base.Predicate
import org.apache.solr.schema.{FieldType, IndexSchema}

/**
 *  A more generified version of sellstome json serializer.
 *  @author Aliaksandr Zhuhrou
 *  @since 1.0
 */
object JSONSerializer {
  
  /** A list of fields that allowed to be serialized into response */
  private val allowedFields = List("id","price")

  /**
   *  Transforms a single entry to a json string
   * @param doc A search result unit
   * @return
   */
  private def docToJson(doc: Document, schema: IndexSchema): JSONObject = {
    val docJson = new JSONObject()
    for ((fieldName, schemaField) <- schema.getFields()) {
       if (schemaField.stored() && allowedFields.contains(schemaField.getName())) { //todo zhugrov - remove filtering after finishing with debugging
         val indexableField = doc.getField(schemaField.getName())
         if (indexableField != null) {
          val fieldType      = schemaField.getType()
          val converter      = FieldConverters.get(fieldType.getClass().asInstanceOf[Class[FieldType]]).get
          docJson.put(schemaField.getName(), converter.toJson(fieldType, indexableField))
         } else if (schemaField.isRequired) {
           throw new NoSuchElementException("Document doesn't contain a field that was defined in schema.xml file " +
             "as a required field. The field name is \"" + fieldName + "\"")
         }
       }
    }
    //todo zhugrov a - add fake fields for now
//    docJson.put("updated_at", "2011-07-07T16:37:33.000Z")
//    docJson.put("author", "vol4ok")
//    docJson.put("avator", new JSONObject().put("name", "av-1").put("type", "png"))
//    docJson.put("count", 12)
//    docJson.put("created_at", "2011-07-07T16:37:33.000Z")
//    docJson.put("images", new JSONArray()
//      .put(new JSONObject()
//      .put("name", "item-1")
//      .put("type", "png")))
    return docJson
  }

  def writeResponse(writer: Writer, req: SolrQueryRequest, rsp: SolrQueryResponse) {
    val jsonDocs = new JSONArray()
    val docs = extractSearchResults(rsp.getValues())
    val docIterator = docs.iterator()
    val searcher = req.getSearcher()
    for (i <- 1 to docs.size()) {
      val docId = docIterator.next()
      val doc = searcher.doc(docId)
      jsonDocs.put(docToJson(doc, req.getSchema()))
    }
    writer.write(jsonDocs.toString())
  }

  /**
   *  Extract a search result list.
   * @param data data to be returned in solr response
   * @throws org.apache.solr.common.SolrException could not parse responseData
   */
  @Nonnull
  protected def extractSearchResults(data: NamedList[_]): DocList = {
    Iterables.find(data.asInstanceOf[java.lang.Iterable[Map.Entry[String, _]]], new Predicate[Map.Entry[String, _]] {
      def apply(entry: Map.Entry[String, _]): Boolean = entry.getValue match {
        case resultContext: ResultContext => true
        case _ => false
      }
    }).getValue.asInstanceOf[ResultContext].docs;
  }
}