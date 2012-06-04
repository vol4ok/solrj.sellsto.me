package sellstome.lucene.codecs

import org.apache.lucene.codecs.lucene40.Lucene40Codec
import org.apache.lucene.codecs.DocValuesFormat

/**
 * Ads support for a mutable doc values.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesCodec extends Lucene40Codec {

  private val docValues: DocValuesFormat = new MutableDocValuesFormat()

  /** Encodes/decodes doc-values */
  override def docValuesFormat(): DocValuesFormat = docValues

}