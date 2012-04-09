package sellstome.lucene.io.packed.array

import sellstome.BaseUnitTest
import collection.mutable
import collection.mutable.HashMap
import sellstome.control.trysuppress

/**
 * Tests the [[sellstome.lucene.io.packed.array.ConjugateArraysSorter]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class ConjugateArraysSorterUnitTest extends BaseUnitTest {

  test("test quicksort normal case") {
    for (i <- 0 until 50) {
      testQuickSort[Byte]
      testQuickSort[Short]
      testQuickSort[Int]
      testQuickSort[Long]
      testQuickSort[Float]
      testQuickSort[Double]
    }
  }

  test("test quicksort: duplicate detection") {
    for (i <- 0 until 100) {
      val ords = createDuplicates(newOrdArray(1000))
      val values = newValuesArray[Long](1000)
      try {
        newSorter[Long](ords, values).quickSort()
        fail()
      } catch {
        case e: IllegalStateException => {}
      }
    }
  }

  protected def testQuickSort[T](implicit m: Manifest[T]) {
    val size    = 100 + nextInt(900)
    val ords    = newOrdArray(size)
    val values  = newValuesArray[T](size)
    val fuse    = fuseArrays[T](ords, values)
    newSorter[T](ords, values).quickSort()
    validateSorting[T](ords, values, fuse)
  }

  test("test mergesort normal case") {
    for (i <- 0 until 50) {
      testMergeSort[Byte]
      testMergeSort[Short]
      testMergeSort[Int]
      testMergeSort[Long]
      testMergeSort[Float]
      testMergeSort[Double]
    }
  }

  test("test mergesort duplicates detection") {
    for (i <- 0 until 100) {
      val ords = createDuplicates(newOrdArray(1000))
      val values = newValuesArray[Long](1000)
      try {
        newSorter[Long](ords, values).mergeSort()
        fail()
      } catch {
        case e: IllegalStateException => {}
      }
    }
  }

  protected def testMergeSort[T](implicit m: Manifest[T]) {
    val size    = 100 + nextInt(900)
    val ords    = newOrdArray(size)
    val values  = newValuesArray[T](size)
    val fuse    = fuseArrays[T](ords, values)
    newSorter[T](ords, values).mergeSort()
    validateSorting[T](ords, values, fuse)
  }

  test("test insertion sort") {
    for (i <- 0 until 50) {
      testInsertionSort[Byte]
      testInsertionSort[Short]
      testInsertionSort[Int]
      testInsertionSort[Long]
      testInsertionSort[Float]
      testInsertionSort[Double]
    }
  }

  test("test insertion sort: duplicates values") {
    for (i <- 0 until 100) {
      val ords    = createDuplicates(newOrdArray(1000))
      val values  = newValuesArray[Long](1000)
      try {
        newSorter[Long](ords, values).insertionSort()
        fail()
      } catch {
        case e: IllegalStateException => {}
      }
    }
  }

  protected def testInsertionSort[T](implicit m: Manifest[T]) {
    val size    = 10 + nextInt(90)
    val ords    = newOrdArray(size)
    val values  = newValuesArray[T](size)
    val fuse    = fuseArrays[T](ords, values)
    newSorter[T](ords, values).insertionSort()
    validateSorting[T](ords, values, fuse)
  }

  protected def validateSorting[T](ords: Array[Int], values: Array[T], fuse: mutable.Map[Int, T]) {
    assert(ords.length == values.length)
    assert(fuse.size == ords.length, s"fuseSize: ${fuse.size} and ordsSize: ${ords.length}")

    for (i <- 0 until ords.length) {
      if (i != 0) {
        assert(ords(i) > ords(i - 1))
      }
      assert(fuse(ords(i)) == values(i))
    }
  }

  protected def newSorter[T](ords: Array[Int], vals: Array[T]): ConjugateArraysSorter[T]
    = new ConjugateArraysSorter[T](ords, vals)

  protected def fuseArrays[T](ords: Array[Int], vals: Array[T]): mutable.Map[Int,T] = {
    assert(ords.length == vals.length)
    val fusion = new HashMap[Int, T]()
    for (i <- 0 until ords.length) {
      fusion.put(ords(i), vals(i))
    }
    return fusion
  }

  /**
   * Uses a randomly choosen two available method to generate a new ords array
   * @see newOrdGapArray
   * @see newOrdOutOfOrderArray
   */
  protected def newOrdArray(size: Int): Array[Int]
    = if (nextBoolean()) newOrdGapArray(size) else newOrdOutOfOrderArray(size)

  /** Creates a new ord array of a given size where values are already ordered */
  protected def newOrdGapArray(size: Int): Array[Int] = {
    val array = new Array[Int](size)
    for (i <- 0 until size) {
      val gap = 1 + nextInt(99)
      if (i == 0) array.update(i, gap) else array.update(i, array(i - 1) + gap)
    }
    return array
  }

  /** Creates a new ord array with out of order ord ordering */
  protected def newOrdOutOfOrderArray(size: Int): Array[Int] = {
    val array = new Array[Int](size)
    val valuesSeen = new mutable.HashSet[Int]()
    while(valuesSeen.size < size) {
      val ord = nextInt(size * 2)
      if (!valuesSeen(ord)) {
        array.update(valuesSeen.size, ord)
        valuesSeen.add(ord)
      }
    }
    return array
  }

  /** Modifies a given array so it will have a duplicates values */
  protected def createDuplicates(ords: Array[Int]): Array[Int] = {
    while (true) {
      val i = nextInt(ords.length)
      val j = nextInt(ords.length)
      if (i != j) {
        ords.update(i, ords(j))
        return ords
      }
    }
    throw new IllegalStateException("should not be here.")
  }

  /** Creates a new primitive arrays */
  protected def newValuesArray[T](size: Int)(implicit m: Manifest[T]) : Array[T] = {
    val array = m.newArray(size)
    if (m.erasure == classOf[Int]) {
      for (i <- 0 until size) {
        array.update(i, nextInt(1000).asInstanceOf[T])
      }
    } else if (m.erasure == classOf[Long]) {
      for (i <- 0 until size) {
        array.update(i, nextLong().asInstanceOf[T])
      }
    } else if (m.erasure == classOf[Short]) {
      for (i <- 0 until size) {
        array.update(i, nextInt(1000).toShort.asInstanceOf[T])
      }
    } else if (m.erasure == classOf[Byte]) {
      for (i <- 0 until size) {
        array.update(i, nextInt(100).toByte.asInstanceOf[T])
      }
    } else if (m.erasure == classOf[Float]) {
      for (i <- 0 until size) {
        array.update(i, (nextDouble() * 1000).toFloat.asInstanceOf[T])
      }
    } else if (m.erasure == classOf[Double]) {
      for (i <- 0 until size) {
        array.update(i, (nextDouble() * 1000).asInstanceOf[T])
      }
    }
    return array
  }

}