package sellstome.solr.util

/**
 * Created by IntelliJ IDEA.
 * User: Alexander Zhugrov
 * Date: 03.02.12
 * Time: 7:41
 * Math utility functions
 */
object Math {

  def pow(a: Long, b: Long): Long = {
    if (b < 0) throw new IllegalArgumentException("Power should be positive number")
    var index = 0
    var result = 1l
    while (index < b) {
      result = result * b
      index = index + 1
    }
    return result
  }

}
