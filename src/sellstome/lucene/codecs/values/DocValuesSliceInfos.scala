package sellstome.lucene.codecs.values

import org.apache.lucene.store.Directory
import scala.collection.mutable
import javax.annotation.Nonnull

/**
 * Allows fetch the information about the slice info objects from the persistent storage.
 * Also uses the file system to read the actual information.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfos extends DVInfosFileSystemSupport {

  /** used to name new slices */
  protected var counter = 0
  /** current infos generation. Should not be nodified directly. */
  protected var generation = DVSliceFilesSupport.DefaultGeneration
  /** used to hold a current set of slices */
  protected val slices = new mutable.ArrayBuffer[DocValuesSliceInfo]()

  /** Reads the information from the file system */
  def read(docValuesId: String, dir: Directory) {
    new FindDocValuesSliceInfos(docValuesId, dir).find[Unit]( (infosFileName) => {
      val infosReader = new DVSliceInfosReaderImpl()
      //infosReader.read(dir, infosFileName, )
    })
  }

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
    ???
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

  /** increment a current generation */
  protected def incGeneration() { generation += 1 }

}