package sellstome.lucene.codecs.values

import org.apache.lucene.store.Directory
import org.apache.lucene.index.FieldInfos
import scala.collection.Set

/**
 * Contains utility operations on doc values
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DocValuesUtils {

  /** obtains a list of files used for a dv storage in a given segment */
  def files(dir: Directory, fieldInfos: FieldInfos, segmentName: String): Set[String]

  /**
   * calculates a unique identifier for a given segment and field
   * @param segmentsName a name of segment
   * @param fieldId a unique identifier a lucene field
   * @return a unique identifier for a dv per segment and field
   */
  def docValuesId(segmentsName: String, fieldId: Int): String

}