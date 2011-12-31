package sellstome.search.solr.request.update

import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update._
import org.apache.noggit.JSONParser
import java.io.StringReader
import collection.mutable.Stack
import org.apache.solr.common.SolrInputDocument
import java.lang.{IllegalStateException, StackOverflowError}


/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/28/11
 * Time: 4:02 PM
 */
object JsonUpdateCommandBuilder {

  /** pass the actual json body that contain a command operand */
  val DocumentParamName = "doc"

  /** Builds add to index command */
  def buildAdd(req: SolrQueryRequest, rsp: SolrQueryResponse): AddUpdateCommand = {

    val params = req.getParams()
    val doc = params.get(DocumentParamName)

    //parse document and convert it to a solr input document
    val addCMD = new AddUpdateCommand(req)
    addCMD.commitWithin = 1000
    addCMD.overwrite = true

    //parses a input file
    val parser = new JSONParser(new StringReader(doc))
    val docStack   = new Stack[SolrInputDocument]()
    val fieldStack = new Stack[String]()
    var cycles = 0
    while(cycles <= 1000) { //avoid infinite loops problem
      parser.nextEvent match {
        case JSONParser.OBJECT_START => {
          docStack.push(new SolrInputDocument())
        }
        case JSONParser.STRING       => {
          if (parser.wasKey()) {
            fieldStack.push(parser.getString)
          } else {
            docStack.pop().addField(fieldStack.pop(), parser.getString())
          }
        }
        case JSONParser.BIGNUMBER    => {
          docStack.pop().addField(fieldStack.pop(), parser.getLong())
        }
        case JSONParser.BOOLEAN      => {
          docStack.pop().addField(fieldStack.pop(), parser.getBoolean())
        }
        case JSONParser.LONG         => {
          docStack.pop().addField(fieldStack.pop(), parser.getLong())
        }
        case JSONParser.NULL         => {
          fieldStack.pop() //just ignore for now and clean up a field stack
        }
        case JSONParser.NUMBER       => {
          docStack.pop().addField(fieldStack.pop(), parser.getDouble())
        }
        case JSONParser.OBJECT_END   => //do nothing for now
        case JSONParser.EOF          => {
          addCMD.solrDoc = docStack.pop()
          return addCMD
        }
      }
      Utils.detectInfiniteLoop(cycles, 1000)
      cycles = cycles + 1
    }

    throw new IllegalStateException("Shouldn't reach this line.")
  }

  /** Builds delete from index command */
  def buildDelete(req: SolrQueryRequest, rsp: SolrQueryResponse): DeleteUpdateCommand = {
    null
  }

  /** Builds merge indexes command */
  def buildMergeIndexes(req: SolrQueryRequest, rsp: SolrQueryResponse): MergeIndexesCommand = {
    null
  }

  /** Builds commit indexes command */
  def buildCommit(req: SolrQueryRequest, rsp: SolrQueryResponse): CommitUpdateCommand = {
    null
  }

  /** Builds rollback from index command */
  def buildRollback(req: SolrQueryRequest, rsp: SolrQueryResponse): RollbackUpdateCommand = {
    null
  }
  

  /** Groups a utility methods that do not have a direct impact on primary logic */
  private object Utils {

    def detectInfiniteLoop(cycle: Int, maxCycle: Int) {
      detectInfiniteLoop(cycle, maxCycle, None)
    }

    /**
     * @throws RuntimeException in case when cycle limit exceed
     */
    def detectInfiniteLoop(cycle: Int, maxCycle: Int, params: Option[Map[Any,Any]]) {
      if (cycle == maxCycle) {
        val message = new StringBuilder()
        message.append("Infinite loop detected! Please check the underlying code.")
        if (params.isDefined) {
          for (param <- params.get) {
            message.append("\n").append("Routine param: ")
                   .append(param._1).append("=").append(param._2)
          }
        }
        throw new RuntimeException(message.toString())
      }
    }


  }

}