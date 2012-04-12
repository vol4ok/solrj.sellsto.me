package sellstome.lucene.io.packed.array

import primitive.PrimitiveList
import org.apache.lucene.store.IndexOutput
import org.apache.lucene.util.ArrayUtil
import org.apache.commons.lang.ArrayUtils
import gnu.trove.list.array.{TByteArrayList, TIntArrayList}

/**
 * A generic writer for a sparse array
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class PackedArrayWriter[V](dataType: Type[V]) {
  /** an number of elements in one compression block */
  protected val BlockSize = 8
  /** stores indexes of added elements */
  protected val ords: TIntArrayList = new TIntArrayList()
  /** stores values for added elements */
  private lazy val values: PrimitiveList[V] = dataType.newBuffer()

  /** adds a new element to a list. This operation may proceed out of order */
  def add(ord: Int, value: V) {
    ords.add(ord)
    values.add(value)
  }

  def write(out: IndexOutput) {
    writeHeader(out)
    val ordsRawCopy = ords.toArray()
    val valsRawCopy = values.toArray()
    getSorter(ordsRawCopy, valsRawCopy).mergeSort()
    writeData(out, ordsRawCopy, valsRawCopy)
  }

  /** Writes a header that contains a information about data element size in bytes */
  protected def writeHeader(out: IndexOutput) {
    out.writeInt(dataType.size)
  }

  /** Writes a actual data to a stream */
  protected def writeData(out: IndexOutput, ords: Array[Int], vals: Array[V]) {
    val bytes = new TByteArrayList(dataType.size * BlockSize)
    ords foreach { ord =>
      bytes.reset()
      var mask = 0.toByte


    }
  }

  /**
   * @param ords a ords array. Sorting by this column.
   * @param values a values array
   * @return a new sorter by ord value.
   * @throws IllegalStateException in case if duplicate ord is found
   */
  protected def getSorter(ords: Array[Int], values: Array[V]): ConjugateArraysSorter[V]
      = new ConjugateArraysSorter[V](ords, values)

}