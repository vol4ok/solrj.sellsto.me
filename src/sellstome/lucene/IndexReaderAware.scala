package sellstome.lucene

import org.apache.lucene.index.IndexReader


/**
 *
 * todo zhugrov a - investigate the possibility that we may have non multi-reader
 * @author Aliaksandr Zhuhrou
 *
 */
trait IndexReaderAware {

  def setIndexReader(reader: IndexReader)

}