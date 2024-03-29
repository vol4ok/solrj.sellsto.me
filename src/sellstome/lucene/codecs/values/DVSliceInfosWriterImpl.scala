package sellstome.lucene.codecs.values

import org.apache.lucene.store.{ChecksumIndexOutput, IOContext, Directory, IndexOutput}


/**
 * Default implementation for the slice infos writer
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DVSliceInfosWriterImpl extends DocValuesSliceInfosWriter {

  def writeInfos(dir: Directory, infosFileName: String,
                 infos: DocValuesSliceInfos, context: IOContext): IndexOutput = {
    val output = createOutput(dir, infosFileName, context)
    output.writeLong(infos.currentGeneration())
    output.writeInt(infos.currentCounter())
    output.writeInt(infos.size())
    infos.foreach((slice) => {
      output.writeString(slice.name)
    })
    return output
  }

  def prepareCommit(out: IndexOutput) {
    //for backward compatibility
  }

  def finishCommit(out: IndexOutput) {
    out.asInstanceOf[ChecksumIndexOutput].finishCommit()
    out.close()
  }

  protected def createOutput(dir: Directory, fileName: String, context: IOContext): IndexOutput
     = new ChecksumIndexOutput(dir.createOutput(fileName, context))

}
