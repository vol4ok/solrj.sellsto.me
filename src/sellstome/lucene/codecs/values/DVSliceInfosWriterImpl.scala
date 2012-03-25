package sellstome.lucene.codecs.values

import org.apache.lucene.store.{IOContext, Directory, IndexOutput}

/**
 * Default implementation for the slice infos writer
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DVSliceInfosWriterImpl extends DocValuesSliceInfosWriter {

  def writeInfos(dir: Directory, segmentsFileName: String,
                 infos: DocValuesSliceInfos, context: IOContext): IndexOutput = {

    ???
  }

  def prepareCommit(out: IndexOutput) {}

  def finishCommit(out: IndexOutput) {}
}
