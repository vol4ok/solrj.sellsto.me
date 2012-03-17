package sellstome.lucene.codecs.values

import FindDocValuesSliceInfos.ProgressInfo
import sellstome.{HelperRandomTestException, BaseUnitTest}
import collection.mutable.HashSet
import FindDVSlicesGenMethod.FindDVSlicesGenMethod

/**
 * Tests for [[sellstome.lucene.codecs.values.FindDocValuesSliceInfos]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class FindDocValuesSliceInfosUnitTest extends BaseUnitTest {

  test("ProgressInfo - basic usage pattern") {
    val progressInfo = new ProgressInfo[Boolean]()
    val methodSeen = new HashSet[FindDVSlicesGenMethod]()

    var infiniteLoopWatch = 0
    while(progressInfo.isShouldTryAgain() && infiniteLoopWatch < 1000) {
      methodSeen.add(progressInfo.proposeMethod())
      progressInfo.addGenSeen(nextGeneration())
      try {
        val result = processResultOrError()
        progressInfo.setResult(result)
      } catch {
        case e: HelperRandomTestException => {
          progressInfo.setLastSeenException(e)
        }
      }
      progressInfo.advance()

      infiniteLoopWatch = infiniteLoopWatch + 1
    }

    assert(infiniteLoopWatch != 1000)
    assert(methodSeen.size == 2)
    try {
      val result = progressInfo.getResult()
    } catch {
      case e: HelperRandomTestException => { info(e) }
    }
  }

  var lastGen: Long = 0
  protected def nextGeneration(): Long = {
    if (nextInt(10) > 8) {
      lastGen = lastGen + 1
    }
    return lastGen
  }

  protected def processResultOrError(): Boolean = {
    return if (nextInt(10) >= 8) {
      nextBoolean()
    } else {
      throw new HelperRandomTestException()
    }
  }

}
