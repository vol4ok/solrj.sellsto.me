package sellstome.lucene.codecs.values

import javax.annotation.Nonnull
import org.apache.commons.io.FilenameUtils
import org.apache.lucene.index.SegmentInfo

object DVSliceFilesSupport {
  /** Name of the doc values slices file extension */
  val DVSlicesExtension = "dvslices"
  /** Suffix for the DV slices gen file */
  val GenSuffix = "gen"
  /** Name of the extension for the generation reference file */
  val DVSlicesGenExtension = DVSlicesExtension+"_"+GenSuffix
}

/**
 * Contains methods for operation on dv file names.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DVSliceFilesSupport {

  /**
   * Get the generation of the most recent changes to a given doc values slices
   * Q: should you allows null as the input parameter?
   * @param files array of file names to check
   * @return a generation number
   */
  protected def getLastCommitGeneration(@Nonnull docValuesId: String, @Nonnull files: Array[String]): Option[Long] = {
    return files.foldLeft[Option[Long]](None) {
      (maxGenOrNone, fileName) =>
        if (isDocSlicesFileWithId(fileName, docValuesId)) {
          val gen: Long = genForFileName(fileName)
          maxGenOrNone match {
            case Some(maxGen) => if (gen > maxGen) Some(gen) else maxGenOrNone
            case None         => Some(gen)
          }
        } else {
          maxGenOrNone
        }
    }
  }

  /** Parses the generation off the segments file name and return it. */
  protected def genForFileName(@Nonnull fileName: String): Long = {
    val extension = FilenameUtils.getExtension(fileName)
    if (extension.startsWith(DVSliceFilesSupport.DVSlicesExtension)) {
      return extension.substring((DVSliceFilesSupport.DVSlicesExtension+"_").length()).toLong
    } else {
      throw new IllegalArgumentException("fileName %s is not a doc values slices file".format(fileName))
    }
  }

  /**
   * Computes a full file name from base, extension and generation.
   */
  protected def fileNameFromGeneration(@Nonnull docValuesId: String, @Nonnull gen: Long): String = {
    if (gen == SegmentInfo.WITHOUT_GEN) {
      return docValuesId+"."+DVSliceFilesSupport.DVSlicesExtension
    } else {
      return docValuesId+"."+DVSliceFilesSupport.DVSlicesExtension+"_"+gen.toString
    }
  }

  @inline
  protected def isDocSlicesFileWithId(@Nonnull fileName: String, @Nonnull docValuesId: String): Boolean = {
    val fileBase  = FilenameUtils.getBaseName(fileName)
    if (fileBase == docValuesId) {
      val extension = FilenameUtils.getExtension(fileName)
      return extension.contains(DVSliceFilesSupport.DVSlicesExtension) && extension != DVSliceFilesSupport.DVSlicesGenExtension
    } else {
      return false
    }
  }



}
