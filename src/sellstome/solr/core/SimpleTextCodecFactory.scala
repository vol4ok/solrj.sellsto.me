package sellstome.solr.core

import org.apache.solr.core.CodecFactory
import org.apache.solr.schema.IndexSchema
import org.apache.lucene.codecs.simpletext.SimpleTextCodec
import org.apache.lucene.codecs.Codec

/**
 * Creates a new [[org.apache.lucene.codecs.simpletext.SimpleTextCodec]] instances
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class SimpleTextCodecFactory extends CodecFactory {

  /** @return a codec instance */
  override def getCodec():Codec = new SimpleTextCodec()

}
