package sellstome.lucene.io.packed.array

import sellstome.BaseUnitTest
import collection.mutable
import collection.mutable.HashMap

/**
 * Tests the [[sellstome.lucene.io.packed.array.ConjugateArraysSorter]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class ConjugateArraysSorterUnitTest extends BaseUnitTest
                                    with ArrayGeneratorComponent {

  test("test quicksort normal case") {
    for (i <- 0 until 1000) {
      testQuickSort[Byte]
      testQuickSort[Short]
      testQuickSort[Int]
      testQuickSort[Long]
      testQuickSort[Float]
      testQuickSort[Double]
    }
  }

  test("test quicksort: duplicate ords") {
    for (i <- 0 until 1000) {
      val ords = arrGen.createDuplicates()
      val values = numGen.newNumericArray[Long](1000)
      newSorter[Long](ords, values).quickSort()
    }
  }

  protected def testQuickSort[T](implicit m: Manifest[T]) {
    val size    = 100 + numGen.nextInt(900)
    val ords    = arrGen.newOrdArray(size)
    val values  = numGen.newNumericArray[T](size)
    val fuse    = fuseArrays[T](ords, values)
    newSorter[T](ords, values).quickSort()
    validateSorting[T](ords, values, fuse)
  }

  test("test mergesort normal case") {
    for (i <- 0 until 1000) {
      testMergeSort[Byte]
      testMergeSort[Short]
      testMergeSort[Int]
      testMergeSort[Long]
      testMergeSort[Float]
      testMergeSort[Double]
    }
  }

  test("test mergesort: duplicate ords") {
    for (i <- 0 until 1000) {
      val ords = arrGen.createDuplicates()
      val values = numGen.newNumericArray[Long](1000)
      newSorter[Long](ords, values).mergeSort()
    }
  }

  test("test mergesort: preserve duplicates insertion order") {
    val ords = Array(9, 7, 5, 5, 6, 2, 1)
    val vals = Array(1, 1, 3, 2, 1, 1, 1)
    newSorter[Int](ords, vals).mergeSort()
    val fords = Array(1, 2, 5, 5, 6, 7, 9)
    val fvals = Array(1, 1, 3, 2, 1, 1, 1)
    for (i <- 0 until ords.length) {
      assert(ords(i) == fords(i))
      assert(vals(i) == fvals(i))
    }
  }

  protected def testMergeSort[T](implicit m: Manifest[T]) {
    val size    = 100 + numGen.nextInt(900)
    val ords    = arrGen.newOrdArray(size)
    val values  = numGen.newNumericArray[T](size)
    val fuse    = fuseArrays[T](ords, values)
    newSorter[T](ords, values).mergeSort()
    validateSorting[T](ords, values, fuse)
  }

  test("test insertion sort") {
    for (i <- 0 until 1000) {
      testInsertionSort[Byte]
      testInsertionSort[Short]
      testInsertionSort[Int]
      testInsertionSort[Long]
      testInsertionSort[Float]
      testInsertionSort[Double]
    }
  }

  test("test insertion sort: duplicate ords") {
    for (i <- 0 until 1000) {
      val ords    = arrGen.createDuplicates()
      val values  = numGen.newNumericArray[Long](1000)
      newSorter[Long](ords, values).insertionSort()
    }
  }

  protected def testInsertionSort[T](implicit m: Manifest[T]) {
    val size    = 10 + numGen.nextInt(90)
    val ords    = arrGen.newOrdArray(size)
    val values  = numGen.newNumericArray[T](size)
    val fuse    = fuseArrays[T](ords, values)
    newSorter[T](ords, values).insertionSort()
    validateSorting[T](ords, values, fuse)
  }

  protected def validateSorting[T](ords: Array[Int], values: Array[T], fuse: mutable.Map[Int, List[T]]) {
    assert(ords.length == values.length)
    assert(fuse.size == ords.length, s"fuseSize: ${fuse.size} and ordsSize: ${ords.length}")

    for (i <- 0 until ords.length) {
      if (i != 0) {
        assert(ords(i) >= ords(i - 1))
      }
      assert(fuse(ords(i)).contains(values(i)))
    }
  }

  protected def newSorter[T](ords: Array[Int], vals: Array[T]): ConjugateArraysSorter[T]
    = new ConjugateArraysSorter[T](ords, vals)

  protected def fuseArrays[T](ords: Array[Int], vals: Array[T]): mutable.Map[Int,List[T]] = {
    assert(ords.length == vals.length)
    val fusion = new HashMap[Int, List[T]]()
    for (i <- 0 until ords.length) {
        fusion.put(ords(i), fusion.getOrElse(ords(i), List()) :+ vals(i) )
    }
    return fusion
  }

}