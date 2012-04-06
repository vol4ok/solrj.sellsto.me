package sellstome.solr.request

import org.apache.solr.handler.RequestHandlerBase
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.processor.{UpdateRequestProcessor, UpdateRequestProcessorChain}
import org.apache.solr.common.params.UpdateParams
import update.JsonUpdateCommandBuilder

/**
 * Created by IntelliJ IDEA.
 * User: zhygr
 * Date: 12/28/11
 * Time: 2:41 PM
 * Allows updates of index with documents in json format
 */
class JsonUpdateHandler extends RequestHandlerBase {

  /***/
  object Command {

    val RequestParamName = "action"

    val AddCommand = "add"

    val DeleteCommand = "delete"

    val MergeCommand = "merge"

    val CommitCommand = "commit"

    val RollbackCommand = "rollback"
  }

  /**
   * Handles the request body
   * Parse updates command using JsonUpdateCommandBuilder.
   */
  def handleRequestBody(req: SolrQueryRequest, rsp: SolrQueryResponse) {
    val params = req.getParams
    val updateChainName = params.get(UpdateParams.UPDATE_CHAIN)
    val updateProcessor = req.getCore.getUpdateProcessingChain(updateChainName).createProcessor(req, rsp)
    try {
      val cmd = params.get(Command.RequestParamName)
      cmd match {
        case Command.AddCommand       => updateProcessor.processAdd(JsonUpdateCommandBuilder.buildAdd(req, rsp))
        case Command.CommitCommand    => updateProcessor.processCommit(JsonUpdateCommandBuilder.buildCommit(req, rsp))
        case Command.DeleteCommand    => updateProcessor.processDelete(JsonUpdateCommandBuilder.buildDelete(req, rsp))
        case Command.MergeCommand     => updateProcessor.processMergeIndexes(JsonUpdateCommandBuilder.buildMergeIndexes(req, rsp))
        case Command.RollbackCommand  => updateProcessor.processRollback(JsonUpdateCommandBuilder.buildRollback(req, rsp))
      }
    } finally {
      updateProcessor.finish()
    }
  }

  def getDescription      = "allows update using docs in json format"

  def getSourceId         = "do not allows disclosure"

  def getSource           = "do not allows disclosure"

  override def getVersion = "1.0"

}