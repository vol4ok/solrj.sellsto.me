package sellstome.lucene.io.packed.array

import org.apache.lucene.store.IndexOutput
import gnu.trove.list.array.{TByteArrayList, TIntArrayList}
import java.util.BitSet
import gnu.trove.list.TByteList
import sellstome.collection.primitive.PrimitiveList

/**
 * A generic writer for a sparse array
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class PackedArrayWriter[V](dataType: Type[V]) {
  /** a number of elements in one compression block */
  protected val BlockSize = 8
  /** stores indexes of added elements */
  protected val ords: TIntArrayList = new TIntArrayList()
  /** stores values for added elements */
  protected lazy val values: PrimitiveList[V] = dataType.newBuffer()

  /** adds a new element to a list. This operation may proceed out of order */
  def add(ord: Int, value: V) {
    ords.add(ord)
    values.add(value)
  }

  /** Writes the accumulates values to a provided stream */
  def write(out: IndexOutput) {
    assert(ords.size > 0)
    val ordsRawCopy = ords.toArray()
    val valsRawCopy = values.toArray()
    getSorter(ordsRawCopy, valsRawCopy).mergeSort()
    writeHeader(out, ordsRawCopy, valsRawCopy)
    writeData(  out, ordsRawCopy, valsRawCopy)
  }

  /**
   * Writes a header that contains a information about data element size in bytes
   * @param ords a ord array, assumes that ords array is presorted in asc order
   */
  protected def writeHeader(out: IndexOutput, ords: Array[Int], vals: Array[V]) {
    var duplicatesCount = 0
    var ordWalker = 0
    while(ordWalker < ords.length - 1) {
      if (ords(ordWalker) == ords(ordWalker + 1)) duplicatesCount += 1
      ordWalker += 1
    }
    out.writeInt(dataType.size)
    out.writeInt(ords.length - duplicatesCount)
  }

  /**
   * Writes a actual data to a stream
   * Q: should we filter out the duplicates on this stage or it should be implemented on read stage?
   * @param out  an output where data is being written
   * @param ords an ords array. We assume that ords array is sorted in asc order
   * @param vals an values array
   */
  protected def writeData(out: IndexOutput, ords: Array[Int], vals: Array[V]) {
    assert(ords.length == vals.length)
    val currentBlockBytes = new TByteArrayList(dataType.size * BlockSize)
    val prevEl = new {
      var ord = -1
      var arrayWalker = -1
    }
    var arrayWalker = 0
    val blockInfo = new BitSet(8)      //edge case when we finish the writing offset but not write out the value
    while(arrayWalker < ords.length) {
      val ord   = ords(arrayWalker)
      val value = vals(arrayWalker)
      val gap   = ord - prevEl.ord

      if (currentBlockBytes.size == (BlockSize * dataType.size) && !(gap == 0 && (arrayWalker - prevEl.arrayWalker != 0))) {
        writeBlock(out, currentBlockBytes, blockInfo)
        //reset the block bytes storage and the blockInfo
        currentBlockBytes.reset()
        blockInfo.clear()
      }

      if (gap > 1) {
        //then we should encode the gap value
        currentBlockBytes.add(dataType.gapToBytes(gap))
        prevEl.ord = ord
        prevEl.arrayWalker = arrayWalker
      } else if ((gap == 0 && (arrayWalker - prevEl.arrayWalker) == 0) || gap == 1) {
        //that mean that we has written a gap and in this cycle should write a value
        currentBlockBytes.add(dataType.valToBytes(value))
        prevEl.ord = ord
        prevEl.arrayWalker = arrayWalker
        arrayWalker += 1
        blockInfo.set((currentBlockBytes.size / dataType.size)  - 1) //mark that we have a value for a given index in the current block
      } else if (gap == 0 && (arrayWalker - prevEl.arrayWalker) != 0) {
        //we have a duplicate elements and should rewrite a previous value with a new one
        var startReplaceOffset = currentBlockBytes.size - dataType.size
        currentBlockBytes.set(startReplaceOffset, dataType.valToBytes(value))
        prevEl.ord = ord
        prevEl.arrayWalker = arrayWalker
        arrayWalker += 1
      } else {
        throw new IllegalStateException(s"ord: $ord, value: $value, gap: $gap, arrayWalker: $arrayWalker")
      }
    }

    if (!currentBlockBytes.isEmpty) { //we should not write final segment if it is not empty
      val toFill = (BlockSize * dataType.size) - currentBlockBytes.size //we should fill the last bytes with zero
      if (toFill > 0) currentBlockBytes.addAll(new Array[Byte](toFill))
      writeBlock(out, currentBlockBytes, blockInfo)
    }
  }

  /** writes a given block to a [[org.apache.lucene.store.IndexOutput]] */
  protected def writeBlock(out: IndexOutput, blockBytes: TByteList, blockInfo: BitSet) {
    //we should write a given block
    val blockInfoBytes = blockInfo.toByteArray //warning: an array cloning
    if (blockInfoBytes.length == 1)
      out.writeByte(blockInfoBytes(0))
    else if (blockInfoBytes.length == 0)
      out.writeByte(0.toByte)
    else
      throw new IllegalStateException("byte blockInfo representation is more than one byte")
    out.writeBytes(blockBytes.toArray, blockBytes.size)
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