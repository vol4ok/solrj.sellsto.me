package sellstome.lucene.store

import sellstome.BaseUnitTest
import org.apache.lucene.store.{IndexInput, IndexOutput}

/**
 * Tests the utility for correctness
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class TunneledIOUnitTest extends BaseUnitTest {

  test("test basic in/out") {
    for (i <- 0 until 1000) {
      val bytes = newNumberArray[Byte](1000)
      val (out, in) = newFactory.newPair()

      if (nextBoolean())
        writeByOneByte(out, bytes)
      else
        writeBatch(out, bytes)

      val bytesRead = if (nextBoolean())
        readByOne(in, bytes.length)
      else
        readBatch(in, bytes.length)

      assertArrayEqual[Byte](bytes, bytesRead)
    }
  }

  protected def newFactory: TunneledIOFactory = new TunneledIOFactory()

  protected def writeByOneByte(out: IndexOutput, bytes: Array[Byte]) {
    for (i <- 0 until bytes.length) {
      out.writeByte(bytes(i))
    }
  }

  protected def writeBatch(out: IndexOutput, bytes: Array[Byte]) {
    out.writeBytes(bytes, 0, bytes.length)
  }

  protected def readByOne(in: IndexInput, length: Int): Array[Byte] = {
    val dataIn = new Array[Byte](length)
    for (i <- 0 until length) {
      dataIn.update(i, in.readByte())
    }
    return dataIn
  }

  protected def readBatch(in: IndexInput, length: Int): Array[Byte] = {
    val dataIn = new Array[Byte](length)
    in.readBytes(dataIn, 0, length)
    return dataIn
  }
}
