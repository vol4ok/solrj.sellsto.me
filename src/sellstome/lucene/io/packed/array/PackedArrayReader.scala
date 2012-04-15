package sellstome.lucene.io.packed.array

import org.apache.lucene.store.IndexInput
import gnu.trove.list.array.{TByteArrayList, TIntArrayList}
import java.util.BitSet
import gnu.trove.list.TByteList
import sellstome.collection.primitive.PrimitiveList

/**
 * A generic reader for sparse array format.
 * A sparse array format compression could be represented as following
 * The input array should be presorted in asc order. The presorted array could
 * contain a duplicates. We write only the actual values to the output stream. Also we write gaps in
 * ords between adjusted values.
 * Each block contains a 8 slots.
 * Each block slot has size specific for a given datatype
 * @see [[sellstome.lucene.io.packed.array.Type.size]]
 * Each block starts with one byte value (<b>Descriptor</b>) where each bit describes all slots for a given block
 * If a bit at position <i>i</i> in a Descriptor is set that means that slot at position <i>i</i> encodes a value for a given block
 * If a bit at position <i>i</i> in a Descriptor is unset that means that slot at position <i>i</i> encodes a gap for ords at adjusted positions
 * In case if the gap for ords between adjusted positions is equal to 1 we ommit the gap value and simply write the next value
 * If the ord the first element is not a 0 we write a gap first relative to the -1 ord value
 * In case if we write gap as the last value for a given block then the first value in the next block would be the adjusted
 * value for a given block in this sense the blocks depends on each other.
 * note: not intended for multi-threaded use
 * note: this version should support initialization from multiple sources
 * Q: should we support the non required dv? It affects the merge operation.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class PackedArrayReader[V](dataType: Type[V]) {
  /**
   * a number of elements in one compression block
   * @see [[sellstome.lucene.io.packed.array.PackedArrayWriter]]
   */
  protected val BlockSize = 8
  /** stores indexes of added elements */
  protected val ords: TIntArrayList = new TIntArrayList()
  /** stores values for added elements */
  protected lazy val vals: PrimitiveList[V] = dataType.newBuffer()

  /**
   * Reads a dv data from persistent storage (may be memory too).
   * We assume that data written by a [[sellstome.lucene.io.packed.array.PackedArrayWriter]]
   * is written in asc order.
   * @param dataInputs a collection inputs for a slices in a given docValuesId
   */
  def load(dataInputs: Seq[IndexInput]) {
    assert(dataInputs.size > 0)
    val slicesOrds = new Array[Array[Int]](dataInputs.size)
    val slicesVals = dataType.newArray2(dataInputs.size)
    var slicesWalker = 0
    while(slicesWalker < slicesOrds.length) {
      val (ords, vals) = readSlice(dataInputs(slicesWalker))
      slicesOrds(slicesWalker) = ords
      slicesVals(slicesWalker) = vals
      slicesWalker += 1
    }
    mergeSlices(slicesOrds, slicesVals)
  }

  /** @return the underlying ords array */
  def ordsArray: Array[Int] = ords.toArray

  /** @return the underlying values array */
  def valsArray: Array[V] = vals.toArray()

  /**
   * We assume that we have already read the input header.
   * @return a pair with the first element the ords array and the second the vals array
   */
  protected def readSlice(in: IndexInput): (Array[Int], Array[V]) = {
    val size = readHeader(in)
    val ords = new Array[Int](size)
    val vals = dataType.newBuffer()
    val currentBlockBytes = new TByteArrayList(dataType.size * BlockSize)
    var arrayWalker = 0
    var blockWalker = 0 //each block contains a eight slots
    val blockInfo = new BitSet(8)
    val prevTick = new {
      var ord = -1
      var arrayWalker = -1
    }
    readBlock(in, currentBlockBytes, blockInfo)

    while(arrayWalker < size) { //have elements to read
      if (arrayWalker != prevTick.arrayWalker) { //in first tick we resolve the ord
        val gap = if (blockInfo.get(blockWalker)) { 1 } else {
          val gap = dataType.bytesToGap(currentBlockBytes.toArray(blockWalker * dataType.size, dataType.size))
          blockWalker += 1 //in next tick we should read a next element
          gap
        }
        val ord = prevTick.ord + gap
        ords.update(arrayWalker, ord)
        prevTick.ord = ord
        prevTick.arrayWalker = arrayWalker
      } else { //in the second tick we resolve the value
        if (!blockInfo.get(blockWalker)) throw new IllegalStateException(s"arrayWalker: $arrayWalker blockWalker: $blockWalker")
        vals.add(dataType.bytesToValue(currentBlockBytes.toArray(blockWalker * dataType.size, dataType.size)))
        prevTick.arrayWalker = arrayWalker
        arrayWalker += 1
        blockWalker += 1
      }

      if (blockWalker == BlockSize && arrayWalker < size) {
        blockInfo.clear()
        currentBlockBytes.reset()
        readBlock(in, currentBlockBytes, blockInfo)
        blockWalker = 0 //reset the block walker counter
      }
    }

    return (ords, vals.toArray())
  }

  /**
   * Populates a given blockBytes and blockInfo with data
   * @param in an index input
   * @param blockBytes bytes for a given compression block. Assume that is it empty
   */
  protected def readBlock(in: IndexInput, blockBytes: TByteList, blockInfo: BitSet) {
    assert(blockBytes.isEmpty)
    blockInfo.or(BitSet.valueOf(Array(in.readByte()))) //read block info
    val buffer = new Array[Byte](dataType.size * BlockSize)
    in.readBytes(buffer, 0, buffer.length)
    blockBytes.add(buffer)
  }

  /**
   * Reads the data type size in bytes and the number of docs contained in a given slice
   * note: this method should be invoked before we actually read the data
   * @param in an input data stream
   * @return a number of docs contained in a given slice. Should always > 0
   */
  protected def readHeader(in: IndexInput): Int = {
    assert(dataType.size == in.readInt())
    val size = in.readInt()
    assert(size != 0)
    return size
  }

  /**
   * Merges a given data into internal object data structure. So this method actually has side effects.
   * @param slicesOrds an array where an each element represents an ords array for a particular slice. Note
   *                   that the elements with a higher index has a higher priority and should override
   *                   the data in elements with lower index value(last write win rule).
   * @param slicesVals an array where an each element represents a vals array for a particular slice.
   */
  protected def mergeSlices(slicesOrds: Array[Array[Int]], slicesVals: Array[Array[V]]) {
    val pos = new Array[Int](slicesOrds.length)
    val isEnd = new Array[Boolean](slicesOrds.length)
    //we should merge all that we have read into one array
    var isGlobalEnd = false
    while(!isGlobalEnd) {
      var i = 0
      var minOrd = Int.MaxValue
      var sliceForMinOrd = -1
      while(i < slicesOrds.length) {
        if (!isEnd(i) && slicesOrds(i)(pos(i)) <= minOrd) {
          minOrd = slicesOrds(i)(pos(i))
          sliceForMinOrd = i
        }
        i += 1
      }

      ords.add(minOrd)
      vals.add(slicesVals(sliceForMinOrd)(pos(sliceForMinOrd)))

      //update progress
      i = 0
      while (i < slicesOrds.length) {
        if (!isEnd(i) && slicesOrds(i)(pos(i)) == minOrd) {
          pos.update(i, pos(i)+1)
        }
        if (pos(i) == slicesOrds(i).length) {
          isEnd.update(i, true)
        }
        i += 1
      }

      var shouldFinish = true
      i = 0
      while(i < slicesOrds.length) {
        if (!isEnd(i)) shouldFinish = false
        i += 1
      }

      isGlobalEnd = shouldFinish
    }
  }

}