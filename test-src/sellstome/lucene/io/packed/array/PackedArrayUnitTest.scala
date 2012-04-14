package sellstome.lucene.io.packed.array

import sellstome.BaseUnitTest
import org.apache.lucene.store.{IndexInput, IndexOutput}
import sellstome.lucene.store.TunneledIOFactory

/**
 * Tests the [[sellstome.lucene.io.packed.array.PackedArrayWriter]]
 * Tests the [[sellstome.lucene.io.packed.array.PackedArrayReader]]
 * @author Aliaksandr Zhuhrou
 * @since  1.0
 */
class PackedArrayUnitTest extends BaseUnitTest
                          with ArrayGeneratorComponent {

  protected class Writer[V](dataType: Type[V]) extends PackedArrayWriter[V](dataType) {
    def testWrite(out: IndexOutput, ords: Array[Int], vals: Array[V]) {
      writeHeader(out, ords, vals)
      writeData(out, ords, vals)
    }
  }

  protected class Reader[V](dataType: Type[V]) extends PackedArrayReader[V](dataType) {
    def testRead(in: IndexInput): (Array[Int], Array[V]) = readSlice(in)
  }

  test("test simple duplicates case") {
    val ordsWrite = Array(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 4, 10, 100)
    val valsWrite = Array(3, 4, 3, 1, 3, 7, 3, 5, 2, 1, 4, 7, 1, 0,  2,   0)
    val ords = Array(1, 2, 3, 4, 10, 100)
    val vals = Array(3, 7, 1, 0,  2,   0)
    val (out, in) = newIO
    val writer = new Writer(IntType)
    writer.testWrite(out, ordsWrite, valsWrite)
    val reader = new Reader(IntType)
    val (ordsRead, valsRead) = reader.testRead(in)
    assertArrayEqual(ords, ordsRead)
    assertArrayEqual(vals, valsRead)
  }

  test("High load testing without duplicates") {
    for (i <- 0 until 1000) {
      testNoOrdDuplicatesCase[Byte]
      testNoOrdDuplicatesCase[Short]
      testNoOrdDuplicatesCase[Int]
      testNoOrdDuplicatesCase[Long]
      testNoOrdDuplicatesCase[Float]
      testNoOrdDuplicatesCase[Double]
    }
  }

  protected def testNoOrdDuplicatesCase[T](implicit m: Manifest[T]) {
    val size = 100 + numGen.nextInt(10000)
    val ords = arrGen.newOrdGapArray(size)
    val vals = numGen.newNumberArray[T](size)
    val (out, in) = newIO
    val writer = new Writer(Type.getType[T])
    writer.testWrite(out, ords, vals)
    val reader = new Reader(Type.getType[T])
    val (ordsRead, valsRead) = reader.testRead(in)
    assertArrayEqual[Int](ords, ordsRead)
    assertArrayEqual[T](vals, valsRead)
  }

  protected def newIO: (IndexOutput, IndexInput) = new TunneledIOFactory().newPair()

}