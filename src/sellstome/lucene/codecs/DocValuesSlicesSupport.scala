package sellstome.lucene.codecs

import org.apache.lucene.store.{IOContext, Directory}
import org.apache.lucene.index.DocValues.Type
import collection.mutable.HashMap
import org.apache.lucene.util.{Counter, BytesRef}
import values.{PackedIntsMutableDVConsumer, IntsMutableDVConsumer, DocValuesSliceInfo, DocValuesSliceInfos}
import org.apache.lucene.codecs.DocValuesConsumer

/** Companion object */
object MutableDocValuesConsumerFactory {

  /**
   * Factory method to create a [[org.apache.lucene.codecs.DocValuesConsumer]] instance for a given type. This
   * method returns default implementations for each of the different types
   * defined in the [[org.apache.lucene.index.DocValues.Type]]enumeration.
   *
   * @param docValuesId the file name id used to create files within the writer.
   * @param dir the [[org.apache.lucene.store.Directory]] to create the files from.
   * @param bytesUsed a byte-usage tracking reference
   * @return a new [[org.apache.lucene.codecs.DocValuesConsumer]] instance for the given [[org.apache.lucene.index.DocValues.Type]]
   * @throws IOException
   */
  def create(dvType: Type, docValuesId: String, dir: Directory,
              bytesUsed: Counter, context: IOContext): DocValuesConsumer = {
    import Type._
    return dvType match {
      case FIXED_INTS_16  => new IntsMutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case FIXED_INTS_32  => new IntsMutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case FIXED_INTS_64  => new IntsMutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case FIXED_INTS_8   => new IntsMutableDVConsumer(dir, docValuesId, bytesUsed, context, dvType)
      case VAR_INTS       => new PackedIntsMutableDVConsumer(dir, docValuesId, bytesUsed, context)
      case _              => throw new IllegalArgumentException("Unknown Values: " + dvType)
    }
  }
}

object DocValuesSlicesSupport {
  val DVSegmentSuffix = "dv"
}


/**
 * Adds the ability to read and to write the doc values slices
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DocValuesSlicesSupport {
  /** Information about all available slices. Used for generation of new unique slice name. */
  protected val dvSliceInfos: DocValuesSliceInfos = new DocValuesSliceInfos()
  /** An current slice that being written */
  protected var currentSlice: DocValuesSliceInfo = null

  /** Compose a current slice file name */
  protected def currentSliceFileName(docValuesId: String): String = {
     return docValuesId+currentSlice.getName()+"."+DocValuesSlicesSupport.DVSegmentSuffix
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
