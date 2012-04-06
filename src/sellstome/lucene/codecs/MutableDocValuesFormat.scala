package sellstome.lucene.codecs

import org.apache.lucene.codecs.DocValuesFormat
import java.{util => javautil}
import org.apache.lucene.index.{PerDocWriteState, SegmentReadState, SegmentInfo}
import values.{MutableDocValuesUtils, DocValuesUtils}
import scala.collection.JavaConversions._

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

  /** a collection of utility methods for working with doc values */
  protected val dvUtils: DocValuesUtils = MutableDocValuesUtils

  /** consumes doc values and saves them do a disk */
  def docsConsumer(state: PerDocWriteState) = new MutableDocValuesPerDocConsumer(state)

  /** allows reading for doc values from external storage */
  def docsProducer(segmentState: SegmentReadState) = new MutableDocValuesProducer(segmentState)

  /** Populates a list of files that used for a given segment */
  def files(info: SegmentInfo, files: javautil.Set[String]) {
    files.addAll(dvUtils.files(info.dir, info.getFieldInfos, info.name))
  }

}