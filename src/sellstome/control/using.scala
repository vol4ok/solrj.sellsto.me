package sellstome.control

import java.io.Closeable

/**
 * Implement control structure that similar to a C# using statement
 * @author Alexander Zhugrov
 */
object using {

  /**
   * A control structure that performs call to a given resource cleanup on exit.
   * @param resource a given resource that has a close method
   * @param f a closure in which we can manipulate with a given resource
   * @tparam A type of resource that has a close() method
   * @tparam B type of return of closure function
   * @return a result of computation with a given resource
   */
  def apply[A <: Closeable, B](resource: A)(f: (A) => B): B = {
    try {
      f(resource)
    } finally {
      if (resource != null) {
        trysuppress {
          resource.close()
        }
      }
    }
  }

}
