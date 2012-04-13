package sellstome.lucene.io.packed.array

import primitive.PrimitiveList
import org.apache.lucene.store.IndexInput
import org.apache.lucene.index.CorruptIndexException
import gnu.trove.list.array.{TByteArrayList, TIntArrayList}
import java.util.BitSet
import gnu.trove.list.TByteList

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
  protected lazy val values: PrimitiveList[V] = dataType.newBuffer()

  /**
   * Reads a dv data from persistent storage (may be memory too).
   * We assume that data written by a [[sellstome.lucene.io.packed.array.PackedArrayWriter]]
   * is written in asc order.
   * @param dataInputs a collection inputs for a slices in a given docValuesId
   */
  def load(dataInputs: List[IndexInput]) {
    ???
  }

  /** @return the underlying ords array */
  def ordsArray: Array[Int] = ords.toArray

  /** @return the underlying values array */
  def valsArray: Array[V] = values.toArray()

  /** @return a pair with the first element the ords array and the second the vals array */
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

      if (blockWalker == BlockSize) {
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
   * @return a number of docs contained in a given slice
   * @throws CorruptIndexException in cases when data type size written to a given stream is different
   */
  protected def readHeader(in: IndexInput): Int = {
    assert(dataType.size == in.readInt())
    val size = in.readInt()
    assert(size != 0)
    return size
  }





}