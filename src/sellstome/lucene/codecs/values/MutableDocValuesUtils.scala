package sellstome.lucene.codecs.values

import org.apache.lucene.store.Directory
import org.apache.lucene.index.FieldInfos
import scala.collection.JavaConversions._
import scala.collection.Set
import scala.collection.mutable

/**
 * An implementation of [[sellstome.lucene.codecs.values.DocValuesUtils]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
object MutableDocValuesUtils extends DocValuesUtils
                             with DVFilenamesSupport {

  /**
   * Note: definitely this method could be optimized.
   * @param dir access to a flat list of files
   * @param fieldInfos  a collection of [[org.apache.lucene.index.FieldInfo]] (accessible by number or by name)
   * @param segmentName a name for a current segment
   * @return
   */
  def files(dir: Directory, fieldInfos: FieldInfos, segmentName: String): Set[String] = {
    val allFiles = dir.listAll()
    val dvFiles = new mutable.HashSet[String]()
    for (fieldInfo <- fieldInfos) {
      if (fieldInfo.hasDocValues()) {
        val dvId: String = docValuesId(segmentName, fieldInfo.number)
        for (fileName <- allFiles) {
          if (isGenerationFile(dvId, fileName)) {
            dvFiles.add(fileName)
          } else if (isInfosFileWithId(dvId, fileName)) {
            dvFiles.add(fileName)
          } else if (isSliceFile(dvId, fileName)) {
            dvFiles.add(fileName)
          }
        }
      }
    }
    dvFiles.toSet
  }

  def docValuesId(segmentsName: String, fieldId: Int): String = {
    return segmentsName + "_" + fieldId
  }

}