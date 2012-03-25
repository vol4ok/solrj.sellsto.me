package sellstome.lucene.codecs.values

import org.apache.lucene.store.{IOContext, ChecksumIndexInput, Directory}

/**
 * Reads the information about the doc values slices.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DVSliceInfosReaderImpl extends DocValuesSliceInfosReader {

  def read(dir: Directory, infosFileName: String,
           input: ChecksumIndexInput, infos: DocValuesSliceInfos,
           context: IOContext) {
    val generation = input.readLong()
    val counter    = input.readInt()
    val size       = input.readInt()
    infos.resetState(counter, generation)
    var i = 0
    while(i < size) {
      infos.append(readSlicesInfo(dir, input))
      i += 1
    }
  }

  /** Reads information about the one existing slice info */
  protected def readSlicesInfo(dir: Directory, input: ChecksumIndexInput): DocValuesSliceInfo
    = new DocValuesSliceInfo(input.readString())

}