package sellstome.lucene.io.packed.array

import sellstome.BaseUnitTest
import org.apache.lucene.store.{IndexInput, IndexOutput}
import sellstome.lucene.store.TunneledIOFactory
import gnu.trove.list.array.TIntArrayList
import java.util.TreeMap
import collection.mutable.ArrayBuffer

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
    def testMerge(slicesOrds: Array[Array[Int]], slicesVals: Array[Array[V]]) { mergeSlices(slicesOrds, slicesVals) }
  }


  test("test integrate case") {
    testIntegrateCase[Byte]
    testIntegrateCase[Short]
    testIntegrateCase[Int]
    testIntegrateCase[Long]
    testIntegrateCase[Float]
    testIntegrateCase[Double]
  }

  protected def testIntegrateCase[T](implicit m: Manifest[T]) {
    for (tryAttempt <- 0 until 100) {
      val (slicesOrds, slicesVals) = slicesData[T]
      val (mergedOrds, mergedVals) = merge[T](slicesOrds, slicesVals)
      val dataInputs = new ArrayBuffer[IndexInput]
      for (i <- 0 until slicesOrds.length) {
        val (out, in) = newIO
        new Writer[T](Type.getType[T]).testWrite(out, slicesOrds(i), slicesVals(i))
        dataInputs.append(in)
      }
      val reader = new Reader[T](Type.getType[T])
      reader.load(dataInputs)
      val readerOrds = reader.ordsArray
      val readerVals = reader.valsArray

      assertArrayEqual[Int](mergedOrds, readerOrds)
      assertArrayEqual[T](mergedVals, readerVals)
    }
  }

  test("test the merge function") {
    testMerge[Byte]
    testMerge[Short]
    testMerge[Int]
    testMerge[Long]
    testMerge[Float]
    testMerge[Double]
  }

  protected def testMerge[T](implicit m: Manifest[T]) {
    for (tryAttempt <- 0 until 100) {
      val (slicesOrds, slicesVals) = slicesData[T]
      val (mergedOrds, mergedVals) = merge[T](slicesOrds, slicesVals)
      val reader = new Reader[T](Type.getType[T])
      reader.testMerge(slicesOrds, slicesVals)
      val readerOrds = reader.ordsArray
      val readerVals = reader.valsArray

      assertArrayEqual[Int](mergedOrds, readerOrds)
      assertArrayEqual[T](mergedVals, readerVals)
    }
  }


  protected def slicesData[T](implicit m: Manifest[T]): (Array[Array[Int]], Array[Array[T]]) = {
    val numSlices = numGen.nextIntInRange(1, 100)
    val slicesOrds = new Array[Array[Int]](numSlices)
    val slicesVals = new Array[Array[T]](numSlices)
    for (i <- 0 until numSlices) {
      val size = numGen.nextIntInRange(1, 10000)
      slicesOrds.update(i, arrGen.newOrdGapArray(size))
      slicesVals.update(i, numGen.newNumberArray[T](size))
    }
    return (slicesOrds, slicesVals)
  }

  protected def merge[T](slicesOrds: Array[Array[Int]], slicesVals: Array[Array[T]])(implicit m: Manifest[T]): (Array[Int], Array[T]) = {
    import scala.collection.JavaConversions._
    //we should pre-merge them in order to test with output values
    val merged = new TreeMap[Int, T]()
    for (i <- 0 until slicesOrds.length) {
      val ords = slicesOrds(i)
      val vals = slicesVals(i)
      for (j <- 0 until ords.length) {
        merged.put(ords(j), vals(j))
      }
    }
    val mergedLength = merged.navigableKeySet().size()
    val (mergedOrds, mergedVals, size) = merged.navigableKeySet()
      .foldLeft((new Array[Int](mergedLength), new Array[T](mergedLength), 0)) {
      (ordsValsPos, ord) =>
        ordsValsPos._1.update(ordsValsPos._3, ord)
        ordsValsPos._2.update(ordsValsPos._3, merged.get(ord))
        (ordsValsPos._1, ordsValsPos._2, ordsValsPos._3 + 1)
    }
    return (mergedOrds, mergedVals)
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

  test("High load testing with duplicates") {
    testOrdDuplicatesCase[Byte]
    testOrdDuplicatesCase[Short]
    testOrdDuplicatesCase[Int]
    testOrdDuplicatesCase[Long]
    testOrdDuplicatesCase[Float]
    testOrdDuplicatesCase[Double]
  }

  /**
   * Removes the duplicates with last value with same ord win.
   * Assume that ords array is already presorted.
   * @param ords a presorted ords array with duplicates
   * @param vals a tied values array
   * @tparam T a type for values. Should be any number primitive type
   */
  protected def stripOrdDuplicated[T](ords: Array[Int], vals: Array[T])(implicit m: Manifest[T]): (Array[Int], Array[T]) = {
    assert(ords.length == vals.length)
    val ordsBuffer  = new TIntArrayList()
    val valsBuffer  = Type.getType[T].newBuffer()
    var arrayWalker = 0
    while(arrayWalker < ords.length) {
      if (arrayWalker == 0) {
        ordsBuffer.add(ords(arrayWalker))
        valsBuffer.add(vals(arrayWalker))
      } else if (ords(arrayWalker - 1) == ords(arrayWalker)) {
        valsBuffer.set(ordsBuffer.size - 1, vals(arrayWalker))
      } else {
        ordsBuffer.add(ords(arrayWalker))
        valsBuffer.add(vals(arrayWalker))
      }
      arrayWalker += 1
    }

    return (ordsBuffer.toArray, valsBuffer.toArray)
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

  protected def testOrdDuplicatesCase[T](implicit m: Manifest[T]) {
    val size = numGen.nextIntInRange(100, 10000)
    val ordsWrite = arrGen.newOrdGapDuplicatesArray(size)
    val valsWrite = numGen.newNumberArray[T](size)
    val (ordsFiltered, valsFiltered) = stripOrdDuplicated(ordsWrite, valsWrite)
    val (out, in) = newIO
    val writer = new Writer(Type.getType[T])
    writer.testWrite(out, ordsWrite, valsWrite)
    val reader = new Reader(Type.getType[T])
    val (ordsRead, valsRead) = reader.testRead(in)

    assertArrayEqual[Int](ordsFiltered, ordsRead)
    assertArrayEqual[T](valsFiltered, valsRead)
  }

  protected def testNoOrdDuplicatesCase[T](implicit m: Manifest[T]) {
    val size = numGen.nextIntInRange(100, 10000)
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