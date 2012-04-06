package sellstome.lucene.codecs.values

import javax.annotation.Nonnull
import org.apache.commons.io.FilenameUtils
import org.apache.lucene.index.SegmentInfo

/**
 * Contains methods for operation on dv file names.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DVInfosFilenamesSupport {

  /** Name of the doc values slices file extension */
  protected val DVInfosExtension = "dvslices"
  /** Suffix for the DV slices gen file */
  protected val GenSuffix = "gen"
  /** Name of the extension for the generation reference file */
  protected val DVSlicesGenExtension = DVInfosExtension+"_"+GenSuffix

  /**
   * Get the generation of the most recent changes to a given doc values slices
   * Q: should you allows null as the input parameter?
   * @param files array of file names to check
   * @return a generation number
   */
  protected def getLastCommitGeneration(@Nonnull docValuesId: String, @Nonnull files: Array[String]): Option[Long] = {
    return files.foldLeft[Option[Long]](None) {
      (maxGenOrNone, fileName) =>
        if (isInfosFileWithId(docValuesId, fileName)) {
          val gen: Long = generationFor(fileName)
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
  protected def generationFor(@Nonnull fileName: String): Long = {
    val extension = FilenameUtils.getExtension(fileName)
    if (extension == DVInfosExtension) {
      return SegmentInfo.WITHOUT_GEN
    } else if (extension.startsWith(DVInfosExtension)) {
      return extension.substring((DVInfosExtension+"_").length()).toLong
    } else {
      throw new IllegalArgumentException("fileName %s is not a doc values slices file".format(fileName))
    }
  }

  /** File name for generation file that stores a current generation number */
  protected def generationFile(docValuesId: String): String
    = docValuesId+"."+DVSlicesGenExtension

  /**
   * If a given file is a generation file
   * @param docValuesId a generation file should belong to a particular dvId
   */
  protected def isGenerationFile(@Nonnull docValuesId: String, @Nonnull fileName: String): Boolean
    = generationFile(docValuesId) == fileName

  /**
   * Computes a full file name from base, extension and generation.
   */
  protected def fileForGeneration(@Nonnull docValuesId: String, @Nonnull gen: Long): String = {
    if (gen == SegmentInfo.WITHOUT_GEN) {
      return docValuesId+"."+DVInfosExtension
    } else {
      return docValuesId+"."+DVInfosExtension+"_"+gen.toString
    }
  }

  @inline
  protected def isInfosFileWithId(@Nonnull docValuesId: String, @Nonnull fileName: String): Boolean = {
    val fileBase  = FilenameUtils.getBaseName(fileName)
    if (fileBase == docValuesId) {
      val extension = FilenameUtils.getExtension(fileName)
      return extension.startsWith(DVInfosExtension) && extension != DVSlicesGenExtension
    } else {
      return false
    }
  }

}
