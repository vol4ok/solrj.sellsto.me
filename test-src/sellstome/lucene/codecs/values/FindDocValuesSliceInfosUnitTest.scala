package sellstome.lucene.codecs.values

import sellstome.BaseUnitTest
import collection.mutable.HashSet
import FindDVSlicesGenMethod.FindDVSlicesGenMethod

/**
 * Tests for [[sellstome.lucene.codecs.values.FindDocValuesSliceInfos]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class FindDocValuesSliceInfosUnitTest extends BaseUnitTest {

  test("test exceptional case") {
    val container = new FindDocValuesSliceInfos(null, null)
    val progressInfo = new container.ProgressInfo[Boolean]()
    val methodSeen = new HashSet[FindDVSlicesGenMethod]()
    var infiniteLoopWatch = 0
    var fileSystemRetryCount = 0
    while(progressInfo.isShouldTryAgain() && infiniteLoopWatch < 1000) {
      val method = progressInfo.proposeMethod()
      methodSeen.add(method)
      if (method == FindDVSlicesGenMethod.FileSystem) {
        progressInfo.addGenSeen(1)
        fileSystemRetryCount += 1
      }
      try {
        throw new RuntimeException("test exception")
      } catch {
        case e: RuntimeException => {
          progressInfo.setLastSeenException(e)
        }
      }
      progressInfo.advance()
      infiniteLoopWatch = infiniteLoopWatch + 1
    }

    assert(infiniteLoopWatch != 1000)
    assert(methodSeen.size == 2)
    assert(fileSystemRetryCount == 3)
    try {
      progressInfo.getResult()
      fail("should throw a Runtime exc on call to the getResult")
    } catch {
      case e: RuntimeException => info("Catch the exception.")
    }
  }

  test("Sucess case") {
    val container = new FindDocValuesSliceInfos(null, null)
    val progressInfo = new container.ProgressInfo[Boolean]()
    var cycles = 0
    while(progressInfo.isShouldTryAgain()) {
      progressInfo.addGenSeen(1)
      progressInfo.setResult(true)
      progressInfo.advance()
      cycles += 1
    }

    assert(cycles == 1, "we determine the right result in one step")
    assert(progressInfo.getResult().get, "the result for this operation should be true")
  }

}
