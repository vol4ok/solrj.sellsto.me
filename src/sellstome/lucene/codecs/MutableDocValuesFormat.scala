package sellstome.lucene.codecs

import org.apache.lucene.codecs.DocValuesFormat
import java.util.Set
import org.apache.lucene.index.{PerDocWriteState, SegmentReadState, SegmentInfo}

/**
 * An attempt to build a DocValues storage that provides ability to update the doc values without re-indexing the whole
 * document. The main goal of this implementation is to support out of order doc consuming. In a current implementation
 * every new document should has a greater docId than its predecessor, we wanna get rid of this limitation. In case we we
 * have a two record for a given field and a given docId the last value overrides its predecessor. The same with three or more
 * values. In future we may adds a additional caching layer here.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesFormat extends DocValuesFormat {
  /** consumes doc values and saves them do a disk */
  def docsConsumer(state: PerDocWriteState) = new MutableDocValuesConsumer()

  /** allows reading for doc values from external storage */
  def docsProducer(state: SegmentReadState) = new MutableDocValuesProducer()

  /** Populates a list of files that used for a given segment */
  def files(info: SegmentInfo, files: Set[String]) {}

}