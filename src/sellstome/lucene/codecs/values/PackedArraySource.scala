package sellstome.lucene.codecs.values

import org.apache.lucene.store.IndexInput
import org.apache.lucene.index.DocValues.{Type, Source}
import org.apache.lucene.index.DocValues.Type._
import org.apache.lucene.util.{OpenBitSet, BytesRef}
import sellstome.lucene.io.packed.array.{DoubleType, FloatType, LongType, IntType, ShortType, Type => PackedType, ByteType, PackedArrayReader}

trait PackedArraySourceFactory {
  def apply(dvType: Type, dataInputs: Seq[IndexInput]): PackedArraySource
}

/**
 * todo zhugrov a - think on how to avoid a static state
 */
object PackedArraySource extends PackedArraySourceFactory {
  def apply(dvType: Type, dataInputs: Seq[IndexInput]) = dvType match {
    case FIXED_INTS_8 => new BytePackedArraySource(dvType, dataInputs)
    case FIXED_INTS_16 => new ShortPackedArraySource(dvType, dataInputs)
    case FIXED_INTS_32 => new IntPackedArraySource(dvType, dataInputs)
    case FIXED_INTS_64 => new LongPackedArraySource(dvType, dataInputs)
    case FLOAT_32 => new FloatPackedArraySource(dvType, dataInputs)
    case FLOAT_64 => new DoublePackedArraySource(dvType, dataInputs)
    case _ => throw new IllegalArgumentException(s"unsupported dvType: $dvType")
  }
}

/**
 * Implements a [[org.apache.lucene.index.DocValues.Source]]
 * with support for Packed Sparse Array compression format
 * @see [[sellstome.lucene.io.packed.array.PackedArrayWriter]]
 * @see [[sellstome.lucene.io.packed.array.PackedArrayReader]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 * @param dvType a givev doc values data type
 * @param dataInputs a list of inputs for each data slice
 */
abstract class PackedArraySource(dvType: Type, dataInputs: Seq[IndexInput]) extends Source(dvType) {
  /** types of value being read */
  type V
  protected val packedType: PackedType[V]
  protected var values: Array[V] = null
  protected var valuesPresent: OpenBitSet = null

  override def getArray: AnyRef = values

  override def hasArray: Boolean = true

  override def getBytes(docID: Int, ref: BytesRef): BytesRef = toBytes(valFor(docID), ref)

  /** Reads the data */
  protected def load() {
    assert(values == null, "Already loaded")
    assert(valuesPresent == null, "Already loaded")
    var reader = newReader
    reader.load(dataInputs)
    val ords = reader.ordsArray
    val vals = reader.valsArray
    val valuesLength = ords(ords.length - 1) + 1
    values = packedType.newArray(valuesLength)
    valuesPresent = new OpenBitSet(valuesLength.toLong)
    var i = 0
    while(i < ords.length) {
      val ord = ords(i)
      values.update(ord, vals(i))
      valuesPresent.set(ord)
      i += 1
    }
  }

  protected def valFor(docId: Int): V = {
    if (valuesPresent.get(docId)) {
      return values(docId)
    } else {
      throw new IllegalArgumentException(s"value is not set for docId: $docId")
    }
  }

  protected def newReader: PackedArrayReader[V] = new PackedArrayReader(packedType)

  protected def toBytes(value: V, bytesRef: BytesRef): BytesRef
}

class BytePackedArraySource(dvType: Type, dataInputs: Seq[IndexInput]) extends PackedArraySource(dvType, dataInputs) {
  override type V = Byte
  protected override val packedType = ByteType
  override def getInt(docID: Int):Long = valFor(docID).toLong

  protected override def toBytes(value: Byte, bytesRef: BytesRef): BytesRef = {
    if (bytesRef.bytes.length == 0) {
      bytesRef.bytes = new Array[Byte](1)
    }
    bytesRef.bytes(0) = value
    bytesRef.offset = 0
    bytesRef.length = 1
    return bytesRef
  }
}

class ShortPackedArraySource(dvType: Type, dataInputs: Seq[IndexInput]) extends PackedArraySource(dvType, dataInputs) {
  override type V = Short
  protected override val packedType = ShortType
  override def getInt(docID: Int) = valFor(docID).toLong

  override protected def toBytes(value: Short, ref: BytesRef): BytesRef = {
    if (ref.bytes.length < 2) {
      ref.bytes = new Array[Byte](2)
    }
    ref.offset = 0
    ref.bytes(ref.offset) = (value >> 8).toByte
    ref.bytes(ref.offset + 1) = value.toByte
    ref.length = 2
    return ref
  }
}

class IntPackedArraySource(dvType: Type, dataInputs: Seq[IndexInput]) extends PackedArraySource(dvType, dataInputs) {
  override type V = Int
  protected override val packedType = IntType
  override def getInt(docID: Int) = valFor(docID).toLong
  protected def toBytes(value: Int, ref: BytesRef): BytesRef = {
    if (ref.bytes.length < 4) {
      ref.bytes = new Array[Byte](4)
    }
    ref.offset = 0
    ref.bytes(ref.offset) = (value >> 24).toByte
    ref.bytes(ref.offset + 1) = (value >> 16).toByte
    ref.bytes(ref.offset + 2) = (value >> 8).toByte
    ref.bytes(ref.offset + 3) = value.toByte
    ref.length = 4
    return ref
  }
}

class LongPackedArraySource(dvType: Type, dataInputs: Seq[IndexInput]) extends PackedArraySource(dvType, dataInputs) {
  override type V = Long
  protected override val packedType = LongType
  override def getInt(docID: Int) = valFor(docID)

  protected def toBytes(value: Long, ref: BytesRef): BytesRef = {
    if (ref.bytes.length < 8) {
      ref.bytes = new Array[Byte](8)
    }
    ref.offset = 0
    ref.bytes(ref.offset) = (value >> 56).toByte
    ref.bytes(ref.offset + 1) = (value >> 48).toByte
    ref.bytes(ref.offset + 2) = (value >> 40).toByte
    ref.bytes(ref.offset + 3) = (value >> 32).toByte
    ref.bytes(ref.offset + 4) = (value >> 24).toByte
    ref.bytes(ref.offset + 5) = (value >> 16).toByte
    ref.bytes(ref.offset + 6) = (value >> 8).toByte
    ref.bytes(ref.offset + 7) = value.toByte
    ref.length = 8
    return ref
  }
}

class FloatPackedArraySource(dvType: Type, dataInputs: Seq[IndexInput]) extends PackedArraySource(dvType, dataInputs) {
  override type V = Float
  protected override val packedType = FloatType
  override def getFloat(docID: Int) = valFor(docID).toDouble

  protected def toBytes(value: Float, ref: BytesRef): BytesRef = {
    val intValue = java.lang.Float.floatToRawIntBits(value)
    if (ref.bytes.length < 4) {
      ref.bytes = new Array[Byte](4)
    }
    ref.offset = 0
    ref.bytes(ref.offset) = (intValue >> 24).toByte
    ref.bytes(ref.offset + 1) = (intValue >> 16).toByte
    ref.bytes(ref.offset + 2) = (intValue >> 8).toByte
    ref.bytes(ref.offset + 3) = intValue.toByte
    ref.length = 4
    return ref
  }
}

class DoublePackedArraySource(dvType: Type, dataInputs: Seq[IndexInput]) extends PackedArraySource(dvType, dataInputs) {
  override type V = Double
  protected override val packedType = DoubleType
  override def getFloat(docID: Int) = valFor(docID)

  protected def toBytes(value: Double, ref: BytesRef): BytesRef = {
    val longValue = java.lang.Double.doubleToRawLongBits(value)
    if (ref.bytes.length < 8) {
      ref.bytes = new Array[Byte](8)
    }
    ref.offset = 0
    ref.bytes(ref.offset) = (longValue >> 56).toByte
    ref.bytes(ref.offset + 1) = (longValue >> 48).toByte
    ref.bytes(ref.offset + 2) = (longValue >> 40).toByte
    ref.bytes(ref.offset + 3) = (longValue >> 32).toByte
    ref.bytes(ref.offset + 4) = (longValue >> 24).toByte
    ref.bytes(ref.offset + 5) = (longValue >> 16).toByte
    ref.bytes(ref.offset + 6) = (longValue >> 8).toByte
    ref.bytes(ref.offset + 7) = value.toByte
    ref.length = 8
    return ref
  }
}