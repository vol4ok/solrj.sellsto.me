package sellstome.lucene.codecs.values

import org.apache.lucene.store.{IOContext, ChecksumIndexInput, Directory}

/**
 * Reads the information about the doc values slices.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DVSliceInfosReaderImpl extends DocValuesSliceInfosReader {

  def read(directory: Directory, infosFileName: String,
           header: ChecksumIndexInput, infos: DocValuesSliceInfos,
           context: IOContext) {

  }

  /** Reads information about the one existing slice info */
  protected def readSlicesInfo(dir: Directory, input: ChecksumIndexInput) {

  }

}