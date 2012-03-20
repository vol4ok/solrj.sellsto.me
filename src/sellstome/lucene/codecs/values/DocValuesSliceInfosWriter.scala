package sellstome.lucene.codecs.values

import org.apache.lucene.store.{IOContext, Directory, IndexOutput}


/**
 * Class that allows write the doc values slice infos
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class DocValuesSliceInfosWriter {

  /**
   * Write [[sellstome.lucene.codecs.values.DocValuesSliceInfos]] data without closing the output. The returned
   * output will become finished only after a successful completion of
   * "two phase commit" that first calls #prepareCommit(IndexOutput) and
   * then #finishCommit(IndexOutput).
   * @param dir directory to write data to
   * @param segmentsFileName name of the "docValuesId.dvslices_N" file to create
   * @param infos data to write
   * @return an instance of [[org.apache.lucene.store.IndexOutput]] to be used in subsequent "two
   * phase commit" operations as described above.
   * @throws IOException could not write to FS
   */
  def writeInfos(dir: Directory, segmentsFileName: String, infos: DocValuesSliceInfos, context: IOContext): IndexOutput

  /**
   * First phase of the two-phase commit - ensure that all output can be
   * successfully written out.
   * @param out an instance of [[org.apache.lucene.store.IndexOutput]] returned from a previous
   * @throws IOException could not write to FS
   */
  def prepareCommit(out: IndexOutput): Unit

  /**
   * Second phase of the two-phase commit. In this step the output should be
   * finalized and closed.
   * @param out an instance of [[org.apache.lucene.store.IndexOutput]] returned from a previous
   *  call to #writeInfos(Directory, String, DocValuesSliceInfos, IOContext).
   * @throws IOException could not write to FS
   */
  def finishCommit(out: IndexOutput): Unit


}
