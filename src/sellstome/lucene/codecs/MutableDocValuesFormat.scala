package sellstome.lucene.codecs

import org.apache.lucene.codecs.DocValuesFormat
import java.util.Set
import org.apache.lucene.index.{PerDocWriteState, SegmentReadState, SegmentInfo}

/**
 * An attempt to build a DocValues storage that provides ability to update the doc values without re-indexing the whole
 * document. I think we should use memory as primary storage and periodically flush to a disk.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesFormat extends DocValuesFormat {

  def docsConsumer(state: PerDocWriteState) = new MutableDocValuesConsumer()

  def docsProducer(state: SegmentReadState) = new MutableDocValuesProducer()

  /** Populates a list of files that used for a given segment */
  def files(info: SegmentInfo, files: Set[String]) {}

}