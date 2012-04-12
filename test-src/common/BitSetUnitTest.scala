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
        if (nextBoolean()) {
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

}