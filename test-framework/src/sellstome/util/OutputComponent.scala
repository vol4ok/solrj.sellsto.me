package sellstome.util

/**
 * contains a set of utilities for output
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait OutputComponent {

  protected val outputUtils = new OutputUtils()

  class OutputUtils {

    def printArray[T](arr: Array[T]) {
      assert(arr != null)
      arr.foldLeft("") { (prefix, el) =>
        Console.print(prefix)
        Console.print(el)
        ","
      }
      Console.print("\n")
    }

  }

}
