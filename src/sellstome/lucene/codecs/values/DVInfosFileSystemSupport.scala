package sellstome.lucene.codecs.values

import org.apache.lucene.store.Directory

object DVInfosFileSystemSupport {

}


/**
 * Contains common operations for working
 * on doc values slices on a File System.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait DVInfosFileSystemSupport extends DVSliceFilesSupport {

  /** Writes a given slice infos to a directory. */
  protected def write(infos: DocValuesSliceInfos, docValuesId: String, dir: Directory) {
    val fileName = null
  }

}