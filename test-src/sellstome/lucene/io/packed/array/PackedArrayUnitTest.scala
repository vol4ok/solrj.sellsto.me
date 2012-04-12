package sellstome.lucene.io.packed.array

import sellstome.BaseUnitTest
import org.apache.lucene.store.{IndexInput, IndexOutput}
import sellstome.lucene.store.TunneledIOFactory
import org.powermock.reflect.Whitebox

/**
 * Tests the [[sellstome.lucene.io.packed.array.PackedArrayWriter]]
 * Tests the [[sellstome.lucene.io.packed.array.PackedArrayReader]]
 * @author Aliaksandr Zhuhrou
 * @since  1.0
 */
class PackedArrayUnitTest extends BaseUnitTest {

  test("test simple duplicates case") {
    val ords = Array(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 4, 10, 100)
    val vals = Array(3, 4, 3, 1, 3, 7, 3, 5, 2, 1, 4, 7, 1, 0,  2,   0)
    val (out, in) = newIO
    val writer = new PackedArrayWriter(IntType) {
      def testWrite(out: IndexOutput, ords: Array[Int], vals: Array[Int]) {writeData(out, ords, vals)}
    }
    writer.testWrite(out, ords, vals)
  }

  protected def newIO: (IndexOutput, IndexInput) = new TunneledIOFactory().newPair()

}