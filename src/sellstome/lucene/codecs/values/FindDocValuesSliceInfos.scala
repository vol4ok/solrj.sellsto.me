package sellstome.lucene.codecs.values

import org.apache.lucene.store.Directory
import org.apache.lucene.index.CorruptIndexException
import java.io.IOException
import collection.mutable.{HashSet, Set}


object FindDocValuesSliceInfos {

  val DefaultGenLookaheadCount = 10

}

/**
 * Utility class for executing code that needs to do
 * something with the current slices file.  This is
 * necessary with lock-less commits because from the time
 * you locate the current slices file name, until you
 * actually open it, read its contents, or check modified
 * time, etc., it could have been deleted due to a writer
 * commit finishing.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class FindDocValuesSliceInfos(docValuesId: String, dir: Directory) extends DocValuesSliceFS {

  /**
   * Finds a given slices file and pass its to a
   * @param process
   * @tparam T
   * @return
   */
  @throws(classOf[CorruptIndexException])
  @throws(classOf[IOException])
  def find[T](process: String => T): T = {
    var tryCount = 0
    val genSeen: Set[Long] = new HashSet()
    while(tryAgain(tryCount, genSeen)) {



    }
    return process("")
  }

  /**
   * Determines if should try again
   * @param tryCount a number of executed attempts
   * @param genSeen a queue of seen gen
   * @return
   */
  protected def tryAgain(tryCount: Int, genSeen: Set[Long]): Boolean = {
    throw new NotImplementedError()
  }





}