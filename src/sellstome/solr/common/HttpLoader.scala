package sellstome.solr.common

import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrException.ErrorCode
import runtime.NonLocalReturnControl
import org.apache.commons.io.IOUtils
import java.io.{BufferedReader, InputStreamReader, InputStream, Reader}
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils

/**
 * @todo test this class since migration to a new http client
 * @author Alexander Zhugrov
 */
trait HttpLoader {

  val httpClient: HttpClient = new DefaultHttpClient()

  /**
   * @return a response body as String
   */
  protected def httpGet(uri: String): String = {
    val request = new HttpGet(uri)
    val httpResponse = httpClient.execute(request)
    val status = httpResponse.getStatusLine().getStatusCode()
    if (status != 200) {
      throw new SolrException(ErrorCode.SERVER_ERROR, "Could not retrieve resource: %s . HttpStatusCode: %s".format(uri, status))
    } else {
      return EntityUtils.toString(httpResponse.getEntity())
    }
  }

  /**Retrieves a specified callback */
  protected def httpGetStream[T](uri: String)(callback: (BufferedReader) => T): T = {
    val request = new HttpGet(uri)
    var inputStream: InputStream = null
    try {
      val httpResponse = httpClient.execute(request)
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        inputStream = httpResponse.getEntity().getContent()
        if (inputStream == null) {
          throw new SolrException(ErrorCode.SERVER_ERROR, "Could not retrieve resource: %s . HttpStatusCode: %s".format(uri, httpResponse))
        } else {
          return callback(new BufferedReader(new InputStreamReader(inputStream)))
        }
      } else {
        throw new SolrException(ErrorCode.SERVER_ERROR, "Could not retrieve resource: %s . HttpStatusCode: %s".format(uri, httpResponse))
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