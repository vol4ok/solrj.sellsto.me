package sellstome.lucene.io.packed.array

import sellstome.BaseUnitTest

/**
 * Tests the [[sellstome.lucene.io.packed.array.Type]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class TypesTest extends BaseUnitTest {

  test("compress/decompress methods") {
    testConversionMethods[Byte]
    testConversionMethods[Short]
    testConversionMethods[Int]
    testConversionMethods[Long]
    testConversionMethods[Float]
    testConversionMethods[Double]
  }

  protected def testConversionMethods[T](implicit m: Manifest[T]) {
    val uType = Type.getType[T]
    for (i <- 0 until 1000) {
      val rndNumber = numGen.nextNumber[T]
      val rndGap    = numGen.nextByte().toInt
      val gapBytes  = uType.gapToBytes(rndGap)
      val numBytes  = uType.valToBytes(rndNumber)
      assert(gapBytes.length == uType.size)
      assert(numBytes.length == uType.size)

      val gapFromBytes = uType.bytesToGap(gapBytes)
      val numFromBytes = uType.bytesToValue(numBytes)
      assert(rndGap == gapFromBytes)
      assert(rndNumber == numFromBytes, s"original number: $rndNumber is not equal to the recovered one $numFromBytes for type ${m.erasure}")
    }
  }

  test("conversion double") {
    val number = numGen.nextDouble()
    val numBytes = DoubleType.valToBytes(number)
    val numberFromBytes = DoubleType.bytesToValue(numBytes)
    assert(number == numberFromBytes)
  }


}