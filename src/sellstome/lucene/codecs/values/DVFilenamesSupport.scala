package sellstome.lucene.codecs.values

import org.apache.lucene.store.Directory
import org.apache.lucene.index.{IndexFileNames, FieldInfos}
import java.io.IOException

/**
 * Adds support for operation on docValues file's names
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DVFilenamesSupport extends DVInfosFilenamesSupport {

  /** file name suffix for slices files */
  val DVSegmentSuffix = "dv"

  /** Compose a slice file name */
  protected def sliceFileName(docValuesId: String, slice: String): String
      = docValuesId+slice+"."+DVSegmentSuffix

  /**
   * Checks if a given file list contains a doc values slice file/files for a given
   * field and segment
   * @param docValuesId a dv id (unique per segment and field)
   * @param files a flat list of files (should not contain a null values)
   * @return if a given file list contains a doc values slice file/files
   */
  protected def hasSlices(docValuesId: String, files: Array[String]): Boolean = {
    files exists { fileName => isSliceFile(docValuesId, fileName) }
  }

  protected def isSliceFile(docValuesId: String, fileName: String): Boolean
    = fileName.startsWith(docValuesId) && fileName.endsWith("."+DVSegmentSuffix)

  /**
   * Calculates a set of files that used for storing docValues on the file system.
   * @param files a mutable set that being populated with files where doc values stored
   */
  protected def files(docValuesId: String, files: Array[String]): Array[String] = {
    ???
  }

}