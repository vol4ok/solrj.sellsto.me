package sellstome.lucene.util

/**
 * A general trait for generating a random array data
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait ArrayGeneratorComponent {
  this: SellstomeLuceneTestCase =>

  class ArrayGenerator {
    /** Generates a new numeric array of a given size */
    def newNumericArray[T](size: Int) (implicit arrTag: ArrayTag[T], errTag: ErasureTag[T]): Array[T] = {
      val randArr = arrTag.newArray(size)
      if (errTag.erasure == classOf[Int]) {
        for (i <- 0 until size) {
          randArr.update(i, random.nextInt(1000).asInstanceOf[T])
        }
      } else if (errTag.erasure == classOf[Long]) {
        for (i <- 0 until size) {
          randArr.update(i, random.nextInt(1000).toLong.asInstanceOf[T])
        }
      } else if (errTag.erasure == classOf[Short]) {
        for (i <- 0 until size) {
          randArr.update(i, random.nextInt(1000).toShort.asInstanceOf[T])
        }
      } else if (errTag.erasure == classOf[Byte]) {
        for (i <- 0 until size) {
          randArr.update(i, random.nextInt(1000).toByte.asInstanceOf[T])
        }
      } else if (errTag.erasure == classOf[Float]) {
        for (i <- 0 until size) {
          randArr.update(i, (random.nextDouble() * 1000).toFloat.asInstanceOf[T])
        }
      } else if (errTag.erasure == classOf[Double]) {
        for (i <- 0 until size) {
          randArr.update(i, (random.nextDouble() * 1000).asInstanceOf[T])
        }
      } else {
        throw new IllegalArgumentException(s"Illegal erasure type: ${errTag.erasure}")
      }
      return randArr
    }

  }

}