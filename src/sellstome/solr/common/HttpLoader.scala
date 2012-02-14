package sellstome.solr.common

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrException.ErrorCode
import runtime.NonLocalReturnControl
import org.apache.commons.io.IOUtils
import java.io.{BufferedReader, InputStreamReader, InputStream, Reader}

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 26.01.12
 * Time: 12:46
 *
 */
trait HttpLoader {

  val httpClient: HttpClient = new HttpClient()

  /**
   * @return a response body as String
   */
  protected def httpGet(uri: String): String = {
    val request = new GetMethod(uri)
    val httpStatus = httpClient.executeMethod(request)
    if (httpStatus != 200) {
      throw new SolrException(ErrorCode.SERVER_ERROR, "Could not retrieve resource: %s . HttpStatusCode: %s".format(uri, httpStatus))
    } else {
      return request.getResponseBodyAsString()
    }
  }

  /**Retrieves a specified callback */
  protected def httpGetStream[T](uri: String)(callback: (BufferedReader) => T): T = {
    val request = new GetMethod(uri)
    var inputStream: InputStream = null
    try {
      val httpStatus = httpClient.executeMethod(request)
      if (httpStatus == 200) {
        inputStream = request.getResponseBodyAsStream()
        if (inputStream == null) {
          throw new SolrException(ErrorCode.SERVER_ERROR, "Could not retrieve resource: %s . HttpStatusCode: %s".format(uri, httpStatus))
        } else {
          return callback(new BufferedReader(new InputStreamReader(inputStream)))
        }
      } else {
        throw new SolrException(ErrorCode.SERVER_ERROR, "Could not retrieve resource: %s . HttpStatusCode: %s".format(uri, httpStatus))
      }
    } catch {
      case e: NonLocalReturnControl[_] => throw e
      case e: Throwable => {
        throw e
      }
    } finally {
      IOUtils.closeQuietly(inputStream)
    }
  }

}