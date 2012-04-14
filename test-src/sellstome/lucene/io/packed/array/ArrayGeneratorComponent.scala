package sellstome.lucene.io.packed.array

import collection.mutable
import sellstome.BaseUnitTest

/**
 * Ads functionality for generating data arrays
 * for ords or value
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait ArrayGeneratorComponent {
  this: BaseUnitTest =>

  val arrGen = new ArrayGenerator()

  class ArrayGenerator {

    /**
     * Uses a randomly choosen two available method to generate a new ords array
     * @see newOrdGapArray
     * @see newOrdOutOfOrderArray
     */
    def newOrdArray(size: Int): Array[Int]
      = if (numGen.nextBoolean()) newOrdGapArray(size) else newOrdOutOfOrderArray(size)

    /**
     * Creates a new ord array of a given size where values are already ordered.
     * Do not contains a duplicate ords.
     * @param size a number of elements in generated array
     * @return a ord presorted array without duplicates
     */
    def newOrdGapArray(size: Int): Array[Int] = {
      val array = new Array[Int](size)
      for (i <- 0 until size) {
        val gap = numGen.nextIntInRange(1, 100)
        if (i == 0) array.update(i, gap) else array.update(i, array(i - 1) + gap)
      }
      return array
    }

    /**
     * Creates a new ord array of a given size where values are already ordered.
     * This array may contain a duplicates of various length
     * @param size a number of elements in a generated array
     * @return a ord pre-sorted array with duplicates
     */
    def newOrdGapDuplicatesArray(size: Int): Array[Int] = {
      val arr = new Array[Int](size)
      var arrayWalker = 0

      val dupBlockInfo = new {
        var active = false
        var length = 0
        var pos = 0
        def end: Boolean = pos >= length
        def reset() {
          active = false
          length = 0
          pos = 0
        }
        def activate() {
          active = true
          length = numGen.nextIntInRange(1, 50)
          pos = 0
        }
      }

      while(arrayWalker < size) {
        if (!dupBlockInfo.active && numGen.nextInt(10) < 1) dupBlockInfo.activate()

        if (dupBlockInfo.active) {
          if (arrayWalker == 0) arr.update(arrayWalker, 0) else arr.update(arrayWalker, arr(arrayWalker - 1))
          dupBlockInfo.pos += 1
          if (dupBlockInfo.end) dupBlockInfo.reset()
        } else {
          val gap = numGen.nextIntInRange(1, 100)
          if (arrayWalker == 0) arr.update(arrayWalker, gap) else arr.update(arrayWalker, arr(arrayWalker - 1) + gap)
        }

        arrayWalker += 1
      }

      return arr
    }

    /** Creates a new ord array with out of order ord ordering */
    def newOrdOutOfOrderArray(size: Int): Array[Int] = {
      val array = new Array[Int](size)
      val valuesSeen = new mutable.HashSet[Int]()
      while(valuesSeen.size < size) {
        val ord = numGen.nextInt(size * 2)
        if (!valuesSeen(ord)) {
          array.update(valuesSeen.size, ord)
          valuesSeen.add(ord)
        }
      }
      return array
    }

    /** Modifies a given array so it will have a duplicates values */
    def createDuplicates(): Array[Int] = {
      val ords = newOrdArray(1000)
      while (true) {
        val i = numGen.nextInt(ords.length)
        val j = numGen.nextInt(ords.length)
        if (i != j) {
          ords.update(i, ords(j))
          return ords
        }
      }
      throw new IllegalStateException("should not be here.")
    }

  }

}
