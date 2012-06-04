package sellstome.lucene.codecs

import org.apache.lucene.codecs.PerDocProducerBase
import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.store.{IOContext, Directory}
import java.io.Closeable
import org.apache.lucene.util.IOUtils
import java.util.{TreeMap, Collection}
import java.util.Map
import org.apache.lucene.index.{SegmentReadState, DocValues}
import com.google.common.collect.Maps
import values.DocValuesFactory

/** factory for creating a doc values */
object MutableDocValuesFactory extends DocValuesFactory {

  def docValues(docCount: Int,
                dir: Directory,
                dvId: String,
                dvType: Type,
                context: IOContext): DocValues = {
    import Type._
    dvType match {
      case FIXED_INTS_16  => ???
      case FIXED_INTS_32  => ???
      case FIXED_INTS_64  => ???
      case FIXED_INTS_8   => ???
      case VAR_INTS       => ???
      case _              => throw new IllegalArgumentException("Not supported doc values type: " + dvType)
    }
  }
}

/**
 * An implementation of the [[org.apache.lucene.codecs.PerDocProducer]]
 * that supports mutable docValues.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesProducer(segmentState: SegmentReadState) extends PerDocProducerBase {
  /** stores a doc values for a given segment */
  protected var _docValues: TreeMap[String, DocValues] = null

  /** A factory for creating doc values */
  protected val dvFactory: DocValuesFactory = MutableDocValuesFactory

  protected def docValues(): Map[String, DocValues] = {
    if (_docValues == null) {
      _docValues = initDocValues(segmentState)
    }
    return _docValues
  }

  protected def closeInternal(closeables: Collection[_ <: Closeable]) {
    IOUtils.close(closeables)
  }

  /** Initializes a new DV map for a given segment */
  protected def initDocValues(state: SegmentReadState): TreeMap[String, DocValues] = {
    if (anyDocValuesFields(state.fieldInfos)) {
      return load(state.fieldInfos,
                  state.segmentInfo.name,
                  state.segmentInfo.getDocCount,
                  state.dir,
                  state.context)
    } else {
      return Maps.newTreeMap()
    }
  }

  /** Instantiates a docValues object for a given field and segment */
  protected def loadDocValues(docCount: Int,
                              dir: Directory,
                              docValuesId: String,
                              docValuesType: Type,
                              context: IOContext): DocValues = {
    //Q: too over engineered?
    return dvFactory.docValues(docCount, dir, docValuesId, docValuesType, context)
  }

}