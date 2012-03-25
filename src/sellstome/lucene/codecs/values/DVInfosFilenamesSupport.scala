package sellstome.lucene.codecs.values

import javax.annotation.Nonnull
import org.apache.commons.io.FilenameUtils
import org.apache.lucene.index.SegmentInfo

object DVInfosFilenamesSupport {
  /** Name of the doc values slices file extension */
  val DVInfosExtension = "dvslices"
  /** Suffix for the DV slices gen file */
  val GenSuffix = "gen"
  /** Name of the extension for the generation reference file */
  val DVSlicesGenExtension = DVInfosExtension+"_"+GenSuffix
}

/**
 * Contains methods for operation on dv file names.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DVInfosFilenamesSupport {

  /**
   * Get the generation of the most recent changes to a given doc values slices
   * Q: should you allows null as the input parameter?
   * @param files array of file names to check
   * @return a generation number
   */
  protected def getLastCommitGeneration(@Nonnull docValuesId: String, @Nonnull files: Array[String]): Option[Long] = {
    return files.foldLeft[Option[Long]](None) {
      (maxGenOrNone, fileName) =>
        if (isInfosFileWithId(fileName, docValuesId)) {
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
    if (extension.startsWith(DVInfosFilenamesSupport.DVInfosExtension)) {
      return extension.substring((DVInfosFilenamesSupport.DVInfosExtension+"_").length()).toLong
    } else {
      throw new IllegalArgumentException("fileName %s is not a doc values slices file".format(fileName))
    }
  }

  /** File name for generation file that stores a current generation number */
  protected def generationFile(docValuesId: String): String
    = docValuesId+"."+DVInfosFilenamesSupport.DVSlicesGenExtension

  /**
   * Computes a full file name from base, extension and generation.
   */
  protected def fileForGeneration(@Nonnull docValuesId: String, @Nonnull gen: Long): String = {
    if (gen == SegmentInfo.WITHOUT_GEN) {
      return docValuesId+"."+DVInfosFilenamesSupport.DVInfosExtension
    } else {
      return docValuesId+"."+DVInfosFilenamesSupport.DVInfosExtension+"_"+gen.toString
    }
  }

  @inline
  protected def isInfosFileWithId(@Nonnull fileName: String, @Nonnull docValuesId: String): Boolean = {
    val fileBase  = FilenameUtils.getBaseName(fileName)
    if (fileBase == docValuesId) {
      val extension = FilenameUtils.getExtension(fileName)
      return extension.contains(DVInfosFilenamesSupport.DVInfosExtension) && extension != DVInfosFilenamesSupport.DVSlicesGenExtension
    } else {
      return false
    }
  }



}
