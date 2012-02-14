package sellstome.solr.common

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 24.01.12
 * Time: 12:30
 * Indicates that this resource should be disposed explicitly
 */
trait Disposable extends AutoCloseable {

  def dispose() {
    close()
  }

}