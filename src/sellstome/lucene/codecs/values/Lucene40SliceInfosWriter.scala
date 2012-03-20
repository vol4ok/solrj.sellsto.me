package sellstome.lucene.codecs.values

import org.apache.lucene.store.{ChecksumIndexOutput, IOContext, Directory, IndexOutput}


/**
 * A default doc values slice infos implementation
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class Lucene40SliceInfosWriter extends DocValuesSliceInfosWriter {

  def writeInfos(dir: Directory, segmentsFileName: String, infos: DocValuesSliceInfos, context: IOContext): IndexOutput = {
    ???
  }

  def prepareCommit(out: IndexOutput) { ??? }

  def finishCommit(out: IndexOutput) { ??? }


  protected def createOutput(dir: Directory, segmentFileName: String, context: IOContext): IndexOutput
                                      = new ChecksumIndexOutput(dir.createOutput(segmentFileName, context))


}