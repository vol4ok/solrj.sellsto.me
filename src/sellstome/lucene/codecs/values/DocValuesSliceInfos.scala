package sellstome.lucene.codecs.values

import scala.collection.mutable
import javax.annotation.Nonnull

/**
 * Allows fetch the information about the slice info objects from the persistent storage.
 * Also uses the file system to read the actual information.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param docValuesId unique id per segment and field
 */
class DocValuesSliceInfos(docValuesId: String) extends DVInfosFileSystemSupport {

  /** used to name new slices */
  protected var counter: Int = 0
  /** current infos generation. Should not be nodified directly. */
  protected var generation: Long = DefaultGeneration
  /** used to hold a current set of slices */
  protected val slices = new mutable.ArrayBuffer[DocValuesSliceInfo]()

  def append(@Nonnull slice: DocValuesSliceInfo) {
    if (slices.contains(slice)) throw new IllegalArgumentException("This slice info is already contained!")
    slices.append(slice)
  }

  /**
   * Iterates over all doc values slice info
   * @param  f a iterator function that takes [[sellstome.lucene.codecs.values.DocValuesSliceInfo]]
   *           as its input.
   * @tparam U The return type for the iterator function.
   */
  def foreach[U](f:DocValuesSliceInfo => U) {
    slices.foreach(f)
  }

  /**
   * Generates a new slice name and increments the counter.
   * Note: this implementation is not thread safe.
   * @return a new slice name.
   */
  def newSliceName(): String = {
    val sliceName = "_" + java.lang.Integer.toString(counter, Character.MAX_RADIX)
    counter += 1
    return sliceName
  }

  /** Gets current item generation */
  def currentGeneration(): Long = generation

  /** Gets current counter */
  def currentCounter(): Int = counter

  /** Gets snapshot for its internal state */
  def currentSnapshot(): Snapshot = new Snapshot(counter, generation)

  def getDocValuesId() = docValuesId

  /** A number of slices */
  def size(): Int = slices.size

  /** Resets an internal object state */
  def resetState(counter: Int, generation: Long) {
    this.counter = counter
    this.generation = generation
    this.slices.clear()
  }

  /** Resets an internal object state */
  def resetState(snapShot: Snapshot) {
    resetState(snapShot.counter, snapShot.generation)
  }

  /** increment a current generation */
  protected def incGeneration() { generation = nextGeneration() }
  /** gets the next generation value */
  protected def nextGeneration(): Long = generation + 1

  /** Represents in-moment snapshot for its internal state */
  class Snapshot(val counter: Int, val generation: Long)

}