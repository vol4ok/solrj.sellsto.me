package sellstome.control

import runtime.NonLocalReturnControl

/**
 * Executes block code in try {} and catch all exceptions except a control one
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
object trysupress {

  /**
   * Executes a given block and catch any exceptions during execution
   * @param f a block of code to execute
   * @tparam T the return type for this block
   * @return a Some with object computed by a given block of code or None if a exception occur
   */
  def apply[T](f: => T): Option[T] = {
    try {
      Some(f)
    } catch {
      case e: NonLocalReturnControl[_] => throw e
      case e: Throwable => None
    }
  }

}
