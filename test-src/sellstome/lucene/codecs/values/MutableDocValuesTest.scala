package sellstome.lucene.codecs.values

import sellstome.lucene.util.SellstomeLuceneTestCase
import sellstome.lucene.codecs.MutableDocValuesFormat
import org.apache.lucene.codecs.{DocValuesConsumer, DocValuesFormat}
import org.apache.lucene.util.{Counter, BytesRef}
import java.io.Reader
import org.apache.lucene.index.{IndexableFieldType, IndexableField, DocValues}
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import org.apache.lucene.codecs.lucene40.values.{Floats, Ints}
import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.store.{FlushInfo, MergeInfo, IOContext, Directory}
import java.util.{Random, Comparator}
import org.apache.lucene.index.FieldInfo

/**
 * todo zhugrov a - classify a type for this test
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesTest extends SellstomeLuceneTestCase {
  /** actual indexing format */
  private val docValuesFormat: DocValuesFormat = new MutableDocValuesFormat()
  private val COMP: Comparator[BytesRef] = BytesRef.getUTF8SortedAsUnicodeComparator

  def testVariableIntsLimits() {

    var minMax          = Array[Array[Long]](           Array(Long.MinValue, Long.MaxValue),
                                                        Array(Long.MinValue + 1, 1),
                                                        Array(-1, Long.MaxValue),
                                                        Array(Long.MinValue, -1),
                                                        Array(1, Long.MaxValue),
                                                        Array(-1, Long.MaxValue - 1),
                                                        Array(Long.MinValue + 2, 1))

    var expectedTypes   = Array[DocValues.Type](        Type.FIXED_INTS_64,
                                                        Type.FIXED_INTS_64,
                                                        Type.FIXED_INTS_64,
                                                        Type.FIXED_INTS_64,
                                                        Type.VAR_INTS,
                                                        Type.VAR_INTS,
                                                        Type.VAR_INTS)

    var valueHolder: DocValueHolder = new DocValueHolder()
    for (i <- 0 until minMax.length) {
      var dir: Directory = newDirectory()
      val writer: DocValuesConsumer = getDocValuesConsumer(dir, "test", DocValues.Type.VAR_INTS)
      valueHolder.numberValue = minMax(i)(0)
      writer.add(0, valueHolder)
      valueHolder.numberValue = minMax(i)(1)
      writer.add(1, valueHolder)
      writer.finish(2)
      var r: DocValues = getDocValues(dir, "test", DocValues.Type.VAR_INTS)
      var source: DocValues.Source = getSource(r)
      assertEquals(i + " with min: " + minMax(i)(0) + " max: " + minMax(i)(1), expectedTypes(i), source.getType)
      assertEquals(minMax(i)(0), source.getInt(0))
      assertEquals(minMax(i)(1), source.getInt(1))
      r.close()
      dir.close()
    }
  }

  def testVInts() {
    testInts(Type.VAR_INTS, 63)
  }

  def testFixedInts() {
    testInts(Type.FIXED_INTS_64, 63)
    testInts(Type.FIXED_INTS_32, 31)
    testInts(Type.FIXED_INTS_16, 15)
    testInts(Type.FIXED_INTS_8, 7)
  }

  def testGetInt8Array() {
    var valueHolder: DocValueHolder = new DocValueHolder()
    var sourceArray: Array[Byte] = Array[Byte](1, 2, 3)
    var dir: Directory = newDirectory()
    val trackBytes: Counter = Counter.newCounter()
    var w: DocValuesConsumer = Ints.getWriter(dir, "test", trackBytes, Type.FIXED_INTS_8, newIOContext(random))
    for (i <- 0 until sourceArray.length) {
      valueHolder.numberValue = sourceArray(i).asInstanceOf[Long]
      w.add(i, valueHolder)
    }
    w.finish(sourceArray.length)
    var r: DocValues = Ints.getValues(dir, "test", sourceArray.length, Type.FIXED_INTS_8, newIOContext(random))
    var source: DocValues.Source = r.getSource
    assertTrue(source.hasArray)
    var loaded: Array[Byte] = (source.getArray.asInstanceOf[Array[Byte]])
    assertEquals(loaded.length, sourceArray.length)
    for (i <- 0 until loaded.length) {
      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
    }
    r.close()
    dir.close()
  }

  def testGetInt16Array() {
    var valueHolder: DocValueHolder = new DocValueHolder
    var sourceArray: Array[Short] = Array[Short](1, 2, 3)
    var dir: Directory = newDirectory()
    val trackBytes: Counter = Counter.newCounter
    var w: DocValuesConsumer = Ints.getWriter(dir, "test", trackBytes, Type.FIXED_INTS_16, newIOContext(random))
    for (i <- 0 until sourceArray.length) {
      valueHolder.numberValue = sourceArray(i).asInstanceOf[Long]
      w.add(i, valueHolder)
    }
    w.finish(sourceArray.length)
    var r: DocValues = Ints.getValues(dir, "test", sourceArray.length, Type.FIXED_INTS_16, newIOContext(random))
    var source: DocValues.Source = r.getSource
    assertTrue(source.hasArray)
    var loaded: Array[Short] = (source.getArray.asInstanceOf[Array[Short]])
    assertEquals(loaded.length, sourceArray.length)
    for (i <- 0 until loaded.length) {
      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
    }
    r.close()
    dir.close()
  }

  def testGetInt64Array() {
    var valueHolder: DocValueHolder = new DocValueHolder()
    var sourceArray: Array[Long] = Array[Long](1, 2, 3)
    var dir: Directory = newDirectory()
    val trackBytes: Counter = Counter.newCounter
    var w: DocValuesConsumer = Ints.getWriter(dir, "test", trackBytes, Type.FIXED_INTS_64, newIOContext(random))
    for (i <- 0 until sourceArray.length) {
      valueHolder.numberValue = sourceArray(i)
      w.add(i, valueHolder)
    }
    w.finish(sourceArray.length)
    var r: DocValues = Ints.getValues(dir, "test", sourceArray.length, Type.FIXED_INTS_64, newIOContext(random))
    var source: DocValues.Source = r.getSource
    assertTrue(source.hasArray)
    var loaded: Array[Long] = (source.getArray.asInstanceOf[Array[Long]])
    assertEquals(loaded.length, sourceArray.length)
    for (i <- 0 until loaded.length) {
      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
    }
    r.close()
    dir.close()
  }

  def testGetInt32Array() {
    var valueHolder: DocValueHolder = new DocValueHolder
    var sourceArray: Array[Int] = Array[Int](1, 2, 3)
    var dir: Directory = newDirectory()
    val trackBytes: Counter = Counter.newCounter
    var w: DocValuesConsumer = Ints.getWriter(dir, "test", trackBytes, Type.FIXED_INTS_32, newIOContext(random))
    for (i <- 0 until sourceArray.length) {
      valueHolder.numberValue = sourceArray(i).asInstanceOf[Long]
      w.add(i, valueHolder)
    }
    w.finish(sourceArray.length)
    var r: DocValues = Ints.getValues(dir, "test", sourceArray.length, Type.FIXED_INTS_32, newIOContext(random))
    var source: DocValues.Source = r.getSource
    assertTrue(source.hasArray)
    var loaded: Array[Int] = (source.getArray.asInstanceOf[Array[Int]])
    assertEquals(loaded.length, sourceArray.length)
    for (i <- 0 until loaded.length) {
      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
    }
    r.close()
    dir.close()
  }

  def testGetFloat32Array() {
    var valueHolder: DocValueHolder = new DocValueHolder
    var sourceArray: Array[Float] = Array[Float](1, 2, 3)
    var dir: Directory = newDirectory()
    val trackBytes: Counter = Counter.newCounter
    var w: DocValuesConsumer = Floats.getWriter(dir, "test", trackBytes, newIOContext(random), Type.FLOAT_32)
    for (i <- 0 until sourceArray.length) {
      valueHolder.numberValue = sourceArray(i)
      w.add(i, valueHolder)
    }
    w.finish(sourceArray.length)
    var r: DocValues = Floats.getValues(dir, "test", 3, newIOContext(random), Type.FLOAT_32)
    var source: DocValues.Source = r.getSource
    assertTrue(source.hasArray)
    var loaded: Array[Float] = (source.getArray.asInstanceOf[Array[Float]])
    assertEquals(loaded.length, sourceArray.length)
    for (i <- 0 until loaded.length) {
      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i), 0.0f)
    }
    r.close()
    dir.close()
  }

  def testGetFloat64Array() {
    var valueHolder: DocValueHolder = new DocValueHolder
    var sourceArray: Array[Double] = Array[Double](1, 2, 3)
    var dir: Directory = newDirectory()
    val trackBytes: Counter = Counter.newCounter
    var w: DocValuesConsumer = Floats.getWriter(dir, "test", trackBytes, newIOContext(random), Type.FLOAT_64)
    for (i <- 0 until sourceArray.length) {
      valueHolder.numberValue = sourceArray(i)
      w.add(i, valueHolder)
    }
    w.finish(sourceArray.length)
    var r: DocValues = Floats.getValues(dir, "test", 3, newIOContext(random), Type.FLOAT_64)
    var source: DocValues.Source = r.getSource
    assertTrue(source.hasArray)
    var loaded: Array[Double] = (source.getArray.asInstanceOf[Array[Double]])
    assertEquals(loaded.length, sourceArray.length)
    for (i <- 0 until loaded.length) {
      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i), 0.0d)
    }
    r.close()
    dir.close()
  }

  def testFloats4() {
    runTestFloats(Type.FLOAT_32)
  }

  def testFloats8() {
    runTestFloats(Type.FLOAT_64)
  }




  protected def testInts(`type`: DocValues.Type, maxBit: Int)() {
    var valueHolder: DocValueHolder = new DocValueHolder()
    var maxV: Long = 1
    val NUM_VALUES: Int = 333 + random.nextInt(333)
    val values: Array[Long] = new Array[Long](NUM_VALUES)
    for (rx <- 1 until maxBit) {
      var dir: Directory = newDirectory()
      val trackBytes: Counter = Counter.newCounter
      var w: DocValuesConsumer = Ints.getWriter(dir, "test", trackBytes, `type`, newIOContext(random))
      for (i <- 0 until NUM_VALUES) {
        val v: Long = random.nextLong % (1 + maxV)
        valueHolder.numberValue = ({
          values(i) = v; values(i)
        })
        w.add(i, valueHolder)
      }
      val additionalDocs: Int = 1 + random.nextInt(9)
      w.finish(NUM_VALUES + additionalDocs)
      assertEquals(0, trackBytes.get)
      var r: DocValues = Ints.getValues(dir, "test", NUM_VALUES + additionalDocs, `type`, newIOContext(random))
      for (iter <- 0 until 2) {
        var s: DocValues.Source = getSource(r)
        assertEquals(`type`, s.getType)
        for (i1 <- 0 until NUM_VALUES) {
          val v: Long = s.getInt(i1)
          assertEquals("index " + i1, values(i1), v)
        }
      }
      r.close()
      dir.close()
      maxV *= 2
    }
  }

  protected def runTestFloats(`type`: DocValues.Type)() {
    var valueHolder: DocValueHolder = new DocValueHolder
    var dir: Directory = newDirectory()
    val trackBytes: Counter = Counter.newCounter
    var w: DocValuesConsumer = Floats.getWriter(dir, "test", trackBytes, newIOContext(random), `type`)
    val NUM_VALUES: Int = 777 + random.nextInt(777)
    val values: Array[Double] = new Array[Double](NUM_VALUES)
    for (i <- 0 until NUM_VALUES) {
      val v: Double = if (`type` eq Type.FLOAT_32) random.nextFloat else random.nextDouble
      valueHolder.numberValue = ({
        values(i) = v; values(i)
      })
      w.add(i, valueHolder)
    }
    val additionalValues: Int = 1 + random.nextInt(10)
    w.finish(NUM_VALUES + additionalValues)
    assertEquals(0, trackBytes.get)
    var r: DocValues = Floats.getValues(dir, "test", NUM_VALUES + additionalValues, newIOContext(random), `type`)
    for (iter <- 0 until 2) {
      var s: DocValues.Source = getSource(r)
      for (i <- 0 until NUM_VALUES) {
        assertEquals("" + i, values(i), s.getFloat(i), 0.0f)
      }
    }
    r.close()
    dir.close()
  }

  protected def newIOContext(random: Random): IOContext = {
    val randomNumDocs: Int = random.nextInt(4192)
    val size: Int = random.nextInt(512) * randomNumDocs
    return random.nextInt(5) match {
      case 0 => IOContext.DEFAULT
      case 1 => IOContext.READ
      case 2 => IOContext.READONCE
      case 3 => new IOContext(new MergeInfo(randomNumDocs, size, true, -1))
      case 4 => new IOContext(new FlushInfo(randomNumDocs, size))
      case _ => IOContext.DEFAULT
    }
  }

  protected def getSource(values: DocValues): DocValues.Source = {
    random.nextInt(5) match {
      case 3 =>
        return values.load
      case 2 =>
        return values.getDirectSource
      case 1 =>
        return values.getSource
      case _ =>
        return values.getSource
    }
  }

  protected def getSortedSource(values: DocValues): DocValues.SortedSource = {
    return getSource(values).asSortedSource
  }

  /** retrieves a new doc values consumer */
  protected def getDocValuesConsumer(dir: Directory, fieldId: String, docType: DocValues.Type): DocValuesConsumer = {
    val fieldInfo = new FieldInfo(fieldId, false, 1, false, true, false, null, docType, null)
    return docValuesFormat.docsConsumer(null).addValuesField(docType, fieldInfo)
  }

  protected def getDocValues(dir: Directory, fieldId: String, docType: DocValues.Type): DocValues = {
    throw new NotImplementedError("this method is not implemented yet")
  }

  class DocValueHolder extends IndexableField {

    def tokenStream(a: Analyzer): TokenStream = {
      return null
    }

    def boost: Float = {
      return 0.0f
    }

    def name: String = {
      return "test"
    }

    def binaryValue: BytesRef = {
      return bytes
    }

    def numericValue: Number = {
      return numberValue
    }

    def stringValue: String = {
      return null
    }

    def readerValue: Reader = {
      return null
    }

    def fieldType: IndexableFieldType = {
      return null
    }

    var bytes: BytesRef = null
    var numberValue: Number = null
    var comp: Comparator[BytesRef] = null
  }


}