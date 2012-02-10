package sellstome.search.solr.common

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 01.01.12
 * Time: 2:22
 * Used for indication that a method is not fully implemented yet
 */
class FeatureNotImplementedException(message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean) extends RuntimeException {

  def this(message: String, cause: Throwable) = this (message, cause, false, true)

  def this(message: String) = this (message, null)

  def this() = this ("This feature is not implemented yet. Sorry.")

}