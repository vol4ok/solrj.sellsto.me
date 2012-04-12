package sellstome.solr.common

import java.io.Reader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.apache.solr.common.SolrException
import javax.xml.xpath.{XPathExpressionException, XPathConstants, XPathFactory}
import org.apache.solr.common.util.XMLErrorLogger
import org.w3c.dom.{NodeList, Node, Document}
import collection.LinearSeq
import collection.mutable.ListBuffer
import runtime.NonLocalReturnControl
import org.apache.solr.util.DOMUtil

object XmlParser {
  /**Its looks like strongly advised to avoid multiple calls to this method. */
  private val xpathFactory = XPathFactory.newInstance()

}

trait XmlParser {

  private implicit val Log = LoggerFactory.getLogger(classOf[XmlParser])

  private val XmlErrorLogger = new XMLErrorLogger(Log)

  private var doc: Document = null

  protected def parse(source: Reader) {

    trylog {
      val documentBuilderFactory = DocumentBuilderFactory.newInstance()
      val inputSource = new InputSource(source)
      val documentBuilder = documentBuilderFactory.newDocumentBuilder()

      documentBuilder.setErrorHandler(XmlErrorLogger)
      try {
        doc = documentBuilder.parse(inputSource)
      } finally {
        IOUtils.closeQuietly(inputSource.getByteStream())
        IOUtils.closeQuietly(inputSource.getCharacterStream())
      }
    }

  }

  /**
   * Evaluates xpath expression
   * @param path a xpath expression
   * @param errIfMissing if we need to throw a error in case if we can't resolve a xpath expression
   * @throws SolrException something wrong
   */
  protected def getNode(path: String, root: Any, errIfMissing: Boolean): Option[Node] = {
    evaluationWrapper[Option[Node]](path) {
      XmlParser.xpathFactory.newXPath().evaluate(path, root, XPathConstants.NODE) match {
        case nd: Node => Some(nd)
        case _ => {
          if (errIfMissing) {
            throw new RuntimeException("Missing " + path)
          }
          else {
            Log.debug("Missing optional " + path)
            None
          }
        }
      }
    }
  }

  protected def getNode(path: String, errIfMissing: Boolean): Option[Node] = getNode(path, doc, errIfMissing)

  /**
   * Evaluates xpath expression
   * @param path a xpath expression
   * @param errIfMissing if we need to throw a error in case if we can't resolve a xpath expression
   * @throws SolrException something wrong
   */
  protected def getNodeList(path: String, errIfMissing: Boolean): Option[NodeList] = {
    evaluationWrapper[Option[NodeList]](path) {
      XmlParser.xpathFactory.newXPath().evaluate(path, doc, XPathConstants.NODESET) match {
        case nd: NodeList => Some(nd)
        case _ => {
          if (errIfMissing) {
            throw new RuntimeException("Missing " + path)
          }
          else {
            Log.debug("Missing optional " + path)
            None
          }
        }
      }
    }
  }

  private def evaluationWrapper[T](path: String)(f: => T): T = {
    try {
      return f
    }
    catch {
      case e: NonLocalReturnControl[_] => throw (e)
      case e: SolrException => throw (e)
      case e: XPathExpressionException => {
        SolrException.log(Log, "Error in xpath", e)
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Error in xpath:" + path, e)
      }
      case e: Throwable => {
        SolrException.log(Log, "Error in xpath", e)
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Error in xpath:" + path, e)
      }
    }
  }

  protected def getVal(path: String, errIfMissing: Boolean): Option[String] = getVal(path, doc, errIfMissing)

  protected def getVal(path: String, root: Any, errIfMissing: Boolean): Option[String] =
    getNode(path, root, errIfMissing).map(DOMUtil.getText(_))

  /**
   * Allows retrieve list of map where each map represent a entity attributes.
   * @param basePath a xpath that resolves a path to a entity root element
   * @param subPaths paths to attributes from a given entity root element. Paths represented by map where
   * a key is a business name of given entity attribute and a value is a xpath relative to the entity node root.
   * @throws SolrException a something goes wrong
   */
  protected def matchXmlList(basePath: String, subPaths: Map[String, String]): LinearSeq[Map[String, String]] = {
    val nodeList = getNodeList(basePath, true).get
    val list = new ListBuffer[Map[String, String]]()
    var index = 0
    while (index < nodeList.getLength()) {
      list.append(getListEntryAttributes(nodeList.item(index), subPaths))
      index = index + 1
    }
    return list.result()
  }

  private def getListEntryAttributes(entity: Node, subPaths: Map[String, String]): Map[String, String] =
    subPaths.mapValues(getVal(_, entity, true).get)

  /**Retrieves the result of xpath expressions or throws exception typically a SolrException */
  protected def matchXML(path: String): String = getVal(path, true).get

  /**Retrieves the result of xpath expression or provided default value */
  protected def matchXML(path: String, default: String): String = getVal(path, false).getOrElse(default)

  protected def matchXMLInt(path: String): Int = Integer.parseInt(getVal(path, true).get)

  protected def matchXMLInt(path: String, default: Int): Int = getVal(path, false).map(Integer.parseInt(_)).getOrElse(default)

  protected def matchXMLBool(path: String): Boolean = getVal(path, true).get.toBoolean

  protected def matchXMLBool(path: String, default: Boolean): Boolean = getVal(path, false)
    .map(_.toBoolean)
    .getOrElse(default)

  protected def matchXMLFloat(path: String): Float = getVal(path, true).get.toFloat

  protected def matchXMLFloat(path: String, default: Float): Float = getVal(path, false)
    .map(_.toFloat)
    .getOrElse(default)

  protected def matchXMLDouble(path: String): Double = getVal(path, true).get.toDouble

  protected def matchXMLDouble(path: String, default: Double): Double = getVal(path, false)
    .map(_.toDouble)
    .getOrElse(default)

}