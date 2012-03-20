package sellstome.lucene.codecs.values

import org.apache.lucene.store.{IOContext, ChecksumIndexInput, Directory}

/**
 *
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class Lucene40SliceInfosReader extends DocValuesSliceInfosReader {
  def read(directory: Directory,
           segmentsFileName: String,
           header: ChecksumIndexInput,
           infos: DocValuesSliceInfos,
           context: IOContext) {
    ???
  }
}
