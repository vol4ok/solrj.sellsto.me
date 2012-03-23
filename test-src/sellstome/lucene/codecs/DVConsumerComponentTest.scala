package sellstome.lucene.codecs

import sellstome.lucene.util.SellstomeLuceneTestCase

/**
 * Tests the basic functionality of [[sellstome.lucene.codecs.DocValuesSlicesSupport]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DVConsumerComponentTest extends SellstomeLuceneTestCase {

  /**
   * Test the basic case:
   * 1. Create a new consumer.
   * 2. Add docs
   * 3. Flush and verify that written content could be read.
   */
  def testBaseCase() {
    val dir = newDirectory()
    //todo check that you have everything right with the IOContext

  }

}
