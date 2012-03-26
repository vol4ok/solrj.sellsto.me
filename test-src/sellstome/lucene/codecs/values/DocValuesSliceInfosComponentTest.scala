package sellstome.lucene.codecs.values

import org.junit.Test
import collection.mutable
import sellstome.lucene.util.SellstomeLuceneTestCase

/**
 * Tests [[sellstome.lucene.codecs.values.DocValuesSliceInfos]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class DocValuesSliceInfosComponentTest extends SellstomeLuceneTestCase {

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
      val dir         = newDirectory()
    }

    val infosForWrite = new DocValuesSliceInfos(testData.docValuesId)
    for (i <- 0 until 10) {
      val slice = new DocValuesSliceInfo(infosForWrite.newSliceName())
      testData.slices.append(slice)
      infosForWrite.append(slice)
    }
    infosForWrite.prepareCommit(testData.dir)
    infosForWrite.commit(testData.dir)
    val snapshot = infosForWrite.currentSnapshot()

    val infosForRead = new DocValuesSliceInfos(testData.docValuesId)
    infosForRead.read(testData.dir)

    assertEquals(snapshot.counter, infosForRead.currentCounter())
    assertEquals(snapshot.generation, infosForRead.currentGeneration())
    assertEquals(testData.slices.size, infosForRead.size)
    infosForRead foreach {
      info => assertTrue(testData.slices.contains(info))
    }

    testData.dir.close()
  }

}
