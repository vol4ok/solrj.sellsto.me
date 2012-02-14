package sellstome.solr.common

import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrException.ErrorCode
import javax.annotation.{Nullable, Nonnull}

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 14.10.11
 * Time: 0:15
 * Contains common utility functions.
 */
trait SellstomeSolrComponent {

  /**Check that condition is true and throw a solr exception otherwise */
  @inline
  protected def ensure(cond: Boolean, message: String) {
    if (!cond) throw new SolrException(ErrorCode.SERVER_ERROR, message)
  }

  @inline
  protected def ensure(cond: Boolean, @Nonnull message: String, @Nonnull errorCode: ErrorCode) {
    if (!cond) throw new SolrException(errorCode, message)
  }

  /**
   * Checks that object is not null. Throws a {@link SolrException} otherwise
   * @param obj object to check on non null condition
   * @param message an exception message
   * @param errorCode an solr error code
   */
  @inline
  @Nonnull
  protected def ensureNotNull[T <: AnyRef](@Nullable obj: T, @Nonnull message: String, @Nonnull errorCode: ErrorCode): T = {
    if (obj == null) {
      throw new SolrException(errorCode, message)
    } else {
      return obj
    }
  }


}