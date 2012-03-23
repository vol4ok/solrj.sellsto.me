package sellstome.lucene.codecs.values

import org.apache.lucene.index.IndexFileNames

/**
 * Allows fetch the information about the slice info objects from the persistent storage.
 * Also uses the file system to read the actual information.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfos extends DocValuesSliceFS {

  //used to name new slices
  var counter = 0

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