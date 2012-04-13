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
class PackedArrayUnitTest extends BaseUnitTest {

  test("test simple duplicates case") {
    val ordsWrite = Array(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 4, 10, 100)
    val valsWrite = Array(3, 4, 3, 1, 3, 7, 3, 5, 2, 1, 4, 7, 1, 0,  2,   0)
    val (out, in) = newIO
    val writer = new PackedArrayWriter(IntType) {
      def testWrite(out: IndexOutput, ords: Array[Int], vals: Array[Int]) {
        writeHeader(out, ords, vals)
        writeData(out, ords, vals)
      }
    }
    writer.testWrite(out, ordsWrite, valsWrite)
    val reader = new PackedArrayReader(IntType) {
      def testRead(in: IndexInput): (Array[Int], Array[Int]) = readSlice(in)
    }
    val (ordsRead, valsRead) = reader.testRead(in)
    assertArrayEqual(ordsWrite, ordsRead)
    assertArrayEqual(valsWrite, valsRead)
  }

  protected def newIO: (IndexOutput, IndexInput) = new TunneledIOFactory().newPair()

}