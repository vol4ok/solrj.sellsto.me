package sellstome.lucene.codecs

import org.apache.lucene.store.Directory
import org.apache.lucene.index.DocValues.Type
import values.{DVFilenamesSupport, DocValuesSliceInfo, DocValuesSliceInfos}
import sellstome.control.trysuppress
import sellstome.transactional.TwoPhaseCommit

/**
 * Adds the ability to read and to write the doc values slices
 * todo zhygrov a check this code
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DocValuesSlicesSupport extends DVFilenamesSupport {

  /** implementing class should make accessible the doc values id */
  protected def docValuesId(): String

  /** implementing class should make accessible a directory object */
  protected def dir(): Directory

  /** A current infos */
  protected val slicesInfos: DocValuesSliceInfos = new DocValuesSliceInfos(docValuesId())
  slicesInfos.read(dir())

  /** An current slice that being written */
  protected val currentSlice: DocValuesSliceInfo = new DocValuesSliceInfo(slicesInfos.newSliceName())

  /** Compose a current slice file name that would be used for write */
  protected def currentWriteSliceFileName(docValuesId: String): String = {
    return docValuesId+currentSlice.name+"."+DVSegmentSuffix
  }

  /** Get a latest slice file name that would be used for read. Stop!!! you are wrong. */
  protected def currentReadSliceFileName(docValuesId: String): Option[String] = {
    ???
  }

  /** Flushed a given slices infos to a disk */
  protected def flushSlicesInfos() {
    slicesInfos.append(currentSlice) //maybe encapsulate this changing?
    val commitable: TwoPhaseCommit[Directory] = slicesInfos
    try {
      commitable.prepareCommit(dir())
      commitable.commit(dir())
    } finally {
      trysuppress { commitable.rollbackCommit(dir()) }
    }
  }

  /** The size of fixed size dv value or -1 for the compressed storage. */
  protected def fixedSize(dvType: Type): Int = {
    import Type._
    return dvType match {
      case VAR_INTS       => -1
      case FIXED_INTS_64  =>  8
      case _              =>  throw new IllegalArgumentException("not supported doc values type: %s".format(dvType))
    }
  }

  /** Transforms size to a given doc values type. Inverse of the #fixedSize method. */
  protected def sizeToType(size: Int): Type = size match {
      case 1 => Type.FIXED_INTS_8
      case 2 => Type.FIXED_INTS_16
      case 4 => Type.FIXED_INTS_32
      case 8 => Type.FIXED_INTS_64
      case _ => throw new IllegalStateException("illegal size " + size)
  }

}