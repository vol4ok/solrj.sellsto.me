package sellstome.lucene.io.packed.array

import primitive.PrimitiveList
import gnu.trove.list.array.TIntArrayList
import org.apache.lucene.util.SorterTemplate

/**
 * A generic writer for a sparse array
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class PackedArrayWriter(dataType: Type) {
  /** a type for a given value */
  private type V = dataType.Type
  /** stores indexes of added elements */
  protected val ords: TIntArrayList = new TIntArrayList()
  /** stores values for added elements */
  private lazy val values: PrimitiveList[V] = dataType.newBuffer()

  /** adds a new element to a list. This operation may proceed out of order */
  def add(ord: Int, value: V) {
    ords.add(ord)
    values.add(value)
  }

  protected def sort() {

  }

  /**
   * @param ords a ords array. Sorting by this column.
   * @param values a values array
   * @return a new sorter by ord value.
   * @throws IllegalStateException in case if duplicate ord is found
   */
  protected def getSorter(ords: Array[Int], values: Array[V]): SorterTemplate
      = new ConjugateArraysSorter[V](ords, values)

}