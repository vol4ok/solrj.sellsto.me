package sellstome.lucene.io.packed.array

import primitive._


/**
 * Abstract class that defines a supported data type
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
abstract class Type {
  /** A given primitive type */
  type Type
  /** A size in bytes for a given data type */
  val size: Int
  /** creates a new buffer */
  def newBuffer(): PrimitiveList[Type]
}

object ByteType extends Type {
  type Type = Byte
  val size = 1
  def newBuffer() = new ByteArrayList()
}

object ShortType extends Type {
  type Type = Short
  val size = 2
  def newBuffer() = new ShortArrayList()
}

object IntType extends Type {
  type Type = Int
  val  size = 4
  def newBuffer() = new IntArrayList()
}

object LongType extends Type {
  type Type = Long
  val  size = 8
  def newBuffer() = new LongArrayList()
}

object FloatType extends Type {
  type Type = Float
  val  size = 4
  def newBuffer() = new FloatArrayList()
}

object DoubleType extends Type {
  type Type = Double
  val  size = 8
  def newBuffer() = new DoubleArrayList()
}