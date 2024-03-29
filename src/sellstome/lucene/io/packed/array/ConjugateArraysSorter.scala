package sellstome.lucene.io.packed.array

import org.apache.lucene.util.SorterTemplate

/**
 * Allows sort two conjugated arrays based on first array element ordering
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class ConjugateArraysSorter[T](ords: Array[Int], values: Array[T]) extends SorterTemplate {

  private[this] var pivot: Int = 0

  def mergeSort() {
    mergeSort(0, ords.length - 1)
  }

  def quickSort() {
    quickSort(0, ords.length - 1)
  }

  def insertionSort() {
    insertionSort(0, ords.length - 1)
  }

  /** allows swap conjugate arrays */
  protected def swap(i: Int, j: Int) {
    val ord   = ords(i)
    val value = values(i)
    ords.update(i, ords(j))
    ords.update(j, ord)

    values.update(i, values(j))
    values.update(j, value)
  }

  protected def comparePivot(j: Int): Int = Ordering.Int.compare(pivot, ords(j))

  protected def setPivot(i: Int) { pivot = ords(i) }

  /**
   * A compare method that compares two array values correspondingly at indexes i and j
   * In case if the two elements are equal we compare them by their indexes so later added element will follows the
   * previously added element. We need this behaviour because we require that lately added element should override the
   * previously added element.
   * @param i an index of the first element
   * @param j an index of the second element
   */
  protected def compare(i: Int, j: Int): Int = {
    val comp = Ordering.Int.compare(ords(i), ords(j))
    if (comp == 0)
      Ordering.Int.compare(i, j)
    else
      comp
  }

}