package sellstome.solr.common


/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 05.02.12
 * Time: 12:52
 * Implement control structure that similar to a C# using statement
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
  def apply[A <: {def close()}, B](resource: A)(f: (A) => B): B = {
    try {
      f(resource)
    } finally {
      if (resource != null) try {
        resource.close()
      }
    }
  }

}
