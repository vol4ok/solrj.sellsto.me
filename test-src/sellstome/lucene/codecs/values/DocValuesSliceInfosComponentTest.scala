package sellstome.lucene.codecs.values

import org.apache.lucene.util.LuceneTestCase
import org.junit.Test
import collection.mutable

/**
 * Tests [[sellstome.lucene.codecs.values.DocValuesSliceInfos]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfosComponentTest extends LuceneTestCase {

  /**
   * Tests a base use case:
   * 1. Create infos object.
   * 2. Populate it with slices.
   * 3. Save to a file system.
   * 4. Create another object.
   * 5. Read it from the file system.
   */
  @Test def testBaseUseCase() {
    val testData = new {
      val slices      = new mutable.ArrayBuffer[DocValuesSliceInfo]()
      val docValuesId = "z1_h6"
    }
    val slices = new DocValuesSliceInfos(testData.docValuesId)
    for (i <- 0 until 10) {
      val slice = new DocValuesSliceInfo(slices.newSliceName())
      testData.slices.append(slice)
      slices.append(slice)
    }

  }

}
