package sellstome.lucene.io.packed.array

import sellstome.collection.primitive._


object Type {
  /** get object that represent a given type */
  def getType[T](implicit m: Manifest[T]): Type[T] = {
    if (m.erasure == classOf[Byte]) {
      return ByteType.asInstanceOf[Type[T]]
    } else if (m.erasure == classOf[Short]) {
      return ShortType.asInstanceOf[Type[T]]
    } else if (m.erasure == classOf[Int]) {
      return IntType.asInstanceOf[Type[T]]
    } else if (m.erasure == classOf[Long]) {
      return LongType.asInstanceOf[Type[T]]
    } else if (m.erasure == classOf[Float]) {
      return FloatType.asInstanceOf[Type[T]]
    } else if (m.erasure == classOf[Double]) {
      return DoubleType.asInstanceOf[Type[T]]
    } else {
      throw new IllegalArgumentException(s"Unsupported erasure type: ${m.erasure}")
    }
  }

}

/**
 * Abstract class that defines a supported data type
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class Type[V] {
  /** A size in bytes for a given data type */
  val size: Int
  /** creates a new buffer */
  def newBuffer(): PrimitiveList[V]
  /** used for array creation */
  def newArray2(length: Int): Array[Array[V]]
  /** ord -> byte array */
  def gapToBytes(gap: Int): Array[Byte]
  /** value -> byte array */
  def valToBytes(value: V): Array[Byte]
  /** inverse conversion @see gapToBytes */
  def bytesToGap(bytes: Array[Byte]): Int
  /** inverse conversion @see bytesToValue */
  def bytesToValue(bytes: Array[Byte]): V
}

object ByteType extends Type[Byte] {

  val size = 1

  def newBuffer() = new ByteArrayList()

  def newArray2(length: Int): Array[Array[Byte]] = new Array[Array[Byte]](length)

  def gapToBytes(gap: Int): Array[Byte]       = Array(gap.toByte)

  def valToBytes(value: Byte): Array[Byte]    = Array(value)

  def bytesToGap(bytes: Array[Byte]): Int     = bytes(0).toInt

  def bytesToValue(bytes: Array[Byte]): Byte  = bytes(0)
}

object ShortType extends Type[Short] {

  val size = 2

  def newBuffer() = new ShortArrayList()

  def newArray2(length: Int): Array[Array[Short]] = new Array[Array[Short]](length)

  def gapToBytes(gap: Int): Array[Byte]       = Array(
                                                   (gap     ).toByte,
                                                   (gap >> 8).toByte
                                                )

  def valToBytes(value: Short): Array[Byte]   = Array(
                                                   (value     ).toByte,
                                                   (value >> 8).toByte
                                                )

  def bytesToGap(bytes: Array[Byte]): Int     =  (bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8)

  def bytesToValue(bytes: Array[Byte]): Short = ((bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8)).toShort

}

object IntType extends Type[Int] {

  val  size = 4

  def newBuffer() = new IntArrayList()

  def newArray2(length: Int): Array[Array[Int]] = new Array[Array[Int]](length)

  def gapToBytes(gap: Int): Array[Byte]     = Array(
                                                (gap      ).toByte,
                                                (gap >>  8).toByte,
                                                (gap >> 16).toByte,
                                                (gap >> 24).toByte
                                              )

  def valToBytes(value: Int): Array[Byte]   = Array(
                                                (value      ).toByte,
                                                (value >>  8).toByte,
                                                (value >> 16).toByte,
                                                (value >> 24).toByte
                                              )

  def bytesToGap(bytes: Array[Byte]): Int   = (bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8) | ((bytes(2) & 0xFF) << 16) | ((bytes(3) & 0xFF) << 24)

  def bytesToValue(bytes: Array[Byte]): Int = (bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8) | ((bytes(2) & 0xFF) << 16) | ((bytes(3) & 0xFF) << 24)
}

object LongType extends Type[Long] {

  val  size = 8

  def newBuffer() = new LongArrayList()

  def newArray2(length: Int): Array[Array[Long]] = new Array[Array[Long]](length)

  def gapToBytes(gap: Int): Array[Byte]       = Array(
                                                  (gap      ).toByte,
                                                  (gap >>  8).toByte,
                                                  (gap >> 16).toByte,
                                                  (gap >> 24).toByte,
                                                  0.toByte,
                                                  0.toByte,
                                                  0.toByte,
                                                  0.toByte
                                                )

  def valToBytes(value: Long): Array[Byte]    = Array(
                                                  (value      ).toByte,
                                                  (value >>  8).toByte,
                                                  (value >> 16).toByte,
                                                  (value >> 24).toByte,
                                                  (value >> 32).toByte,
                                                  (value >> 40).toByte,
                                                  (value >> 48).toByte,
                                                  (value >> 56).toByte
                                                )

  def bytesToGap(bytes: Array[Byte]): Int     = (bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8) | ((bytes(2) & 0xFF) << 16) | ((bytes(3) & 0xFF) << 24)

  def bytesToValue(bytes: Array[Byte]): Long  = (bytes(0) & 0xFF).toLong | ((bytes(1) & 0xFF) << 8).toLong | ((bytes(2) & 0xFF).toLong << 16) | ((bytes(3) & 0xFF).toLong << 24) | ((bytes(4) & 0xFF).toLong << 32) | ((bytes(5) & 0xFF).toLong << 40) | ((bytes(6) & 0xFF).toLong << 48) | ((bytes(7) & 0xFF).toLong << 56)
}

object FloatType extends Type[Float] {

  val  size = 4

  def newBuffer() = new FloatArrayList()

  def newArray2(length: Int): Array[Array[Float]] = new Array[Array[Float]](length)

  def gapToBytes(gap: Int): Array[Byte]         = Array(
                                                    (gap      ).toByte,
                                                    (gap >>  8).toByte,
                                                    (gap >> 16).toByte,
                                                    (gap >> 24).toByte
                                                  )

  def valToBytes(value: Float): Array[Byte]     = {
    val raw = java.lang.Float.floatToRawIntBits(value)
    return Array(
      (raw      ).toByte,
      (raw >>  8).toByte,
      (raw >> 16).toByte,
      (raw >> 24).toByte
    )
  }

  def bytesToGap(bytes: Array[Byte]): Int       = (bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8) | ((bytes(2) & 0xFF) << 16) | ((bytes(3) & 0xFF) << 24)

  def bytesToValue(bytes: Array[Byte]): Float   = {
    val raw = (bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8) | ((bytes(2) & 0xFF) << 16) | ((bytes(3) & 0xFF) << 24)
    return java.lang.Float.intBitsToFloat(raw)
  }
}

object DoubleType extends Type[Double] {

  val  size = 8

  def newBuffer() = new DoubleArrayList()

  def newArray2(length: Int): Array[Array[Double]] = new Array[Array[Double]](length)

  def gapToBytes(gap: Int): Array[Byte]         = Array(
                                                    (gap      ).toByte,
                                                    (gap >>  8).toByte,
                                                    (gap >> 16).toByte,
                                                    (gap >> 24).toByte,
                                                    0.toByte,
                                                    0.toByte,
                                                    0.toByte,
                                                    0.toByte
                                                  )

  def valToBytes(value: Double): Array[Byte]    = {
    val raw = java.lang.Double.doubleToRawLongBits(value)
    return Array(
      (raw      ).toByte,
      (raw >>  8).toByte,
      (raw >> 16).toByte,
      (raw >> 24).toByte,
      (raw >> 32).toByte,
      (raw >> 40).toByte,
      (raw >> 48).toByte,
      (raw >> 56).toByte
    )
  }

  def bytesToGap(bytes: Array[Byte]): Int       = (bytes(0) & 0xFF) | ((bytes(1) & 0xFF) << 8) | ((bytes(2) & 0xFF) << 16) | ((bytes(3) & 0xFF) << 24)

  def bytesToValue(bytes: Array[Byte]): Double  = {
    val raw = (bytes(0) & 0xFF).toLong | ((bytes(1) & 0xFF).toLong << 8) | ((bytes(2) & 0xFF).toLong << 16) | ((bytes(3) & 0xFF).toLong << 24) | ((bytes(4) & 0xFF).toLong << 32) | ((bytes(5) & 0xFF).toLong << 40) | ((bytes(6) & 0xFF).toLong << 48) | ((bytes(7) & 0xFF).toLong << 56)
    return java.lang.Double.longBitsToDouble(raw)
  }
}