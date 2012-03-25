package sellstome.lucene.codecs.values

import org.apache.lucene.index.IndexFileNames
import org.apache.lucene.store.Directory

/**
 * Allows fetch the information about the slice info objects from the persistent storage.
 * Also uses the file system to read the actual information.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfos extends DocValuesSliceFS {

  //used to name new slices
  var counter = 0

  /** Reads the information from the file system */
  def read(docValuesId: String, dir: Directory) {
    new FindDocValuesSliceInfos(docValuesId, dir).find[Unit]( (infosFileName) => {
      val infosReader = new DVSliceInfosReaderImpl()
      //infosReader.read(dir, infosFileName, )
    })
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

}