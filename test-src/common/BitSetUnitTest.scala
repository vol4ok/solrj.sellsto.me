package common

import sellstome.BaseUnitTest
import java.util.BitSet

/**
 * Test the contract for classes that being part of standard Java or Scala
 * libraries. Tests experimentally the edge cases for a particular implementations.
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class BitSetUnitTest extends BaseUnitTest {

  test("Test BitSet") {
    val bitSet   = new BitSet(8)
    for (trial <- 0 until 1000) {
      bitSet.clear()
      val bitArray = new Array[Boolean](8)
      for (i <- 0 until 8) {
        if (numGen.nextBoolean()) {
          bitSet.set(i)
          bitArray.update(i, true)
        }
      }
      val bytes = bitSet.toByteArray()
      assert(bytes.length <= 1, s"bytes.length is ${bytes.length}")
      val bitSetFromBytes = BitSet.valueOf(bytes)
      for (i <- 0 until 8) {
        assert(bitSetFromBytes.get(i) == bitArray(i))
      }
    }
  }

  test("test fill method") {
    for (trial <- 0 until 10000) {
      val byte = numGen.nextByte()
      val bitSet = new BitSet(8)
      bitSet.or(BitSet.valueOf(Array(byte)))
      val bytes = bitSet.toByteArray
      if (byte == 0) {
        assert(bytes.length == 0)
      } else {
        assert(bytes.length == 1)
        assert(bytes(0) == byte)
      }
    }
  }

}