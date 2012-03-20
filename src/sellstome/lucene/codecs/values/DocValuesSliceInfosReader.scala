package sellstome.lucene.codecs.values

import org.apache.lucene.store.{IOContext, ChecksumIndexInput, Directory}


/**
 * Specifies an API for classes that can read [[sellstome.lucene.codecs.values.DocValuesSliceInfos]] information.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class DocValuesSliceInfosReader {
  /**
   * Read [[sellstome.lucene.codecs.values.DocValuesSliceInfos]] data from a directory.
   * @param directory directory to read from
   * @param segmentsFileName name of the "docValuesId.dvslices_N" file
   * @param header input of "docValuesId.dvslices_N" file after reading preamble
   * @param infos empty instance to be populated with data
   * @throws IOException when some recoverable exception occurred
   */
  def read(directory: Directory, segmentsFileName: String, header: ChecksumIndexInput, infos: DocValuesSliceInfos, context: IOContext): Unit

}