package sellstome.lucene.codecs.values

import sellstome.lucene.util.SellstomeLuceneTestCase
import sellstome.lucene.codecs.MutableDocValuesFormat
import org.apache.lucene.codecs.{DocValuesConsumer, DocValuesFormat}
import org.apache.lucene.util.{Counter, BytesRef}
import java.io.Reader
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.store.{FlushInfo, MergeInfo, IOContext, Directory}
import java.util.Random
import org.apache.lucene.index._
import org.junit.Ignore

/**
 * todo zhugrov a - classify a type for this test
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesTest extends SellstomeLuceneTestCase {


//  def testVariableIntsLimits() {
//
//    var minMax          = Array[Array[Long]](           Array(Long.MinValue, Long.MaxValue),
//                                                        Array(Long.MinValue + 1, 1),
//                                                        Array(-1, Long.MaxValue),
//                                                        Array(Long.MinValue, -1),
//                                                        Array(1, Long.MaxValue),
//                                                        Array(-1, Long.MaxValue - 1),
//                                                        Array(Long.MinValue + 2, 1))
//
//    var expectedTypes   = Array[DocValues.Type](        Type.FIXED_INTS_64,
//                                                        Type.FIXED_INTS_64,
//                                                        Type.FIXED_INTS_64,
//                                                        Type.FIXED_INTS_64,
//                                                        Type.VAR_INTS,
//                                                        Type.VAR_INTS,
//                                                        Type.VAR_INTS)
//
//    var valueHolder: DocValueHolder = new DocValueHolder()
//    for (i <- 0 until minMax.length) {
//      var dir: Directory = newDirectory()
//      val writer: DocValuesConsumer = getDocValuesConsumer(dir, "test", DocValues.Type.VAR_INTS)
//      valueHolder.numberValue = minMax(i)(0)
//      writer.add(0, valueHolder)
//      valueHolder.numberValue = minMax(i)(1)
//      writer.add(1, valueHolder)
//      writer.finish(2)
//      var r: DocValues = getDocValues(dir, "test", DocValues.Type.VAR_INTS)
//      var source: DocValues.Source = getSource(r)
//      assertEquals(i + " with min: " + minMax(i)(0) + " max: " + minMax(i)(1), expectedTypes(i), source.getType())
//      assertEquals(minMax(i)(0), source.getInt(0))
//      assertEquals(minMax(i)(1), source.getInt(1))
//      r.close()
//      dir.close()
//    }
//  }

  def testFixedInts() {
    testInts(Type.FIXED_INTS_64, 63)
    testInts(Type.FIXED_INTS_32, 31)
    testInts(Type.FIXED_INTS_16, 15)
    testInts(Type.FIXED_INTS_8, 7)
  }

//  @Ignore
//  def testGetInt8Array() {
//    val valueHolder: DocValueHolder = new DocValueHolder()
//    val sourceArray: Array[Byte] = Array[Byte](1, 2, 3)
//    val dir: Directory = newDirectory()
//    val w: DocValuesConsumer = getDocValuesConsumer(dir, "test", Type.FIXED_INTS_8)
//    for (i <- 0 until sourceArray.length) {
//      valueHolder.numberValue = sourceArray(i).asInstanceOf[Long]
//      w.add(i, valueHolder)
//    }
//    w.finish(sourceArray.length)
//    val r: DocValues = getDocValues(dir, "test", Type.FIXED_INTS_8)
//    val source: DocValues.Source = r.getSource()
//    assertTrue(source.hasArray)
//    val loaded: Array[Byte] = (source.getArray.asInstanceOf[Array[Byte]])
//    assertEquals(loaded.length, sourceArray.length)
//    for (i <- 0 until loaded.length) {
//      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
//    }
//    r.close()
//    dir.close()
//  }

//  @Ignore
//  def testGetInt16Array() {
//    val valueHolder: DocValueHolder = new DocValueHolder()
//    val sourceArray: Array[Short] = Array[Short](1, 2, 3)
//    val dir: Directory = newDirectory()
//    val w: DocValuesConsumer = getDocValuesConsumer(dir, "test", Type.FIXED_INTS_16)
//    for (i <- 0 until sourceArray.length) {
//      valueHolder.numberValue = sourceArray(i).asInstanceOf[Long]
//      w.add(i, valueHolder)
//    }
//    w.finish(sourceArray.length)
//    val r: DocValues = getDocValues(dir, "test", Type.FIXED_INTS_16)
//    val source: DocValues.Source = r.getSource
//    assertTrue(source.hasArray)
//    val loaded: Array[Short] = (source.getArray.asInstanceOf[Array[Short]])
//    assertEquals(loaded.length, sourceArray.length)
//    for (i <- 0 until loaded.length) {
//      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
//    }
//    r.close()
//    dir.close()
//  }
//
//  @Ignore
//  def testGetInt64Array() {
//    var valueHolder: DocValueHolder = new DocValueHolder()
//    var sourceArray: Array[Long] = Array[Long](1, 2, 3)
//    var dir: Directory = newDirectory()
//    var w: DocValuesConsumer = getDocValuesConsumer(dir, "test", Type.FIXED_INTS_64)
//    for (i <- 0 until sourceArray.length) {
//      valueHolder.numberValue = sourceArray(i)
//      w.add(i, valueHolder)
//    }
//    w.finish(sourceArray.length)
//    var r: DocValues = getDocValues(dir, "test", Type.FIXED_INTS_64)
//    var source: DocValues.Source = r.getSource()
//    assertTrue(source.hasArray)
//    var loaded: Array[Long] = (source.getArray.asInstanceOf[Array[Long]])
//    assertEquals(loaded.length, sourceArray.length)
//    for (i <- 0 until loaded.length) {
//      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
//    }
//    r.close()
//    dir.close()
//  }
//
//  @Ignore
//  def testGetInt32Array() {
//    val valueHolder: DocValueHolder = new DocValueHolder()
//    val sourceArray: Array[Int] = Array[Int](1, 2, 3)
//    val dir: Directory = newDirectory()
//    val w: DocValuesConsumer = getDocValuesConsumer(dir, "test", Type.FIXED_INTS_32)
//    for (i <- 0 until sourceArray.length) {
//      valueHolder.numberValue = sourceArray(i).asInstanceOf[Long]
//      w.add(i, valueHolder)
//    }
//    w.finish(sourceArray.length)
//    val r: DocValues = getDocValues(dir, "test", Type.FIXED_INTS_32)
//    val source: DocValues.Source = r.getSource()
//    assertTrue(source.hasArray)
//    val loaded: Array[Int] = (source.getArray.asInstanceOf[Array[Int]])
//    assertEquals(loaded.length, sourceArray.length)
//    for (i <- 0 until loaded.length) {
//      assertEquals("value didn't match at index " + i, sourceArray(i), loaded(i))
//    }
//    r.close()
//    dir.close()
//  }

  protected def testInts(docType: DocValues.Type, maxBit: Int)() {
    val valueHolder: DocValueHolder = new DocValueHolder()
    var maxV: Long = 1
    val DocCount: Int = 333 + random.nextInt(333)
    val values = new Array[Long](DocCount)

    for (rx <- 1 until maxBit) {
      val dir: Directory = newDirectory()
      val writer: DocValuesConsumer = getDocValuesConsumer(dir, "test", docType)
      for (docIdWrite <- 0 until DocCount) {
        val value: Long = random.nextLong % (1 + maxV)
        values(docIdWrite) = value
        valueHolder.numberValue = value
        writer.add(docIdWrite, valueHolder)
      }

      val additionalDocs: Int = 1 + random.nextInt(9)
      writer.finish(DocCount + additionalDocs)


      val reader: DocValues = getDocValues(dir, "test", docType)
      for (readIteration <- 0 until 2) {
        val source: DocValues.Source = getSource(reader)
        assertEquals(docType, source.getType())
        for (docIdRead <- 0 until DocCount) {
          val v: Long = source.getInt(docIdRead)
          assertEquals("index " + docIdRead, values(docIdRead), v)
        }
      }

      reader.close()
      dir.close()
      maxV *= 2
    }
  }

  protected def runTestFloats(docType: DocValues.Type)() {
    val valueHolder: DocValueHolder = new DocValueHolder
    val dir: Directory = newDirectory()
    val w: DocValuesConsumer = getDocValuesConsumer(dir, "test", docType)
    val NUM_VALUES: Int = 777 + random.nextInt(777)
    val values: Array[Double] = new Array[Double](NUM_VALUES)
    for (i <- 0 until NUM_VALUES) {
      val v: Double = if (docType eq Type.FLOAT_32) random.nextFloat else random.nextDouble
      valueHolder.numberValue = ({
        values(i) = v; values(i)
      })
      w.add(i, valueHolder)
    }
    val additionalValues: Int = 1 + random.nextInt(10)
    w.finish(NUM_VALUES + additionalValues)
    val r: DocValues = getDocValues(dir, "test", docType)
    for (iter <- 0 until 2) {
      val s: DocValues.Source = getSource(r)
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
        return values.load()
      case 2 =>
        return values.getDirectSource()
      case 1 =>
        return values.getSource()
      case _ =>
        return values.getSource()
    }
  }

  protected def getSortedSource(values: DocValues): DocValues.SortedSource = {
    return getSource(values).asSortedSource
  }

  /**
   * Instantiates a new doc values consumer
   * @param dir provides access to a flat list of files
   * @param fieldId a unique indefinier for DV field
   * @param dvType a data type for a given doc value field
   * @return a new doc values consumer
   */
  protected def getDocValuesConsumer(dir: Directory, fieldId: String, dvType: DocValues.Type): DocValuesConsumer = {
    import DocValues.Type._
    return dvType match {
      case FIXED_INTS_8  => new MutableDVConsumer(dir, fieldId, Counter.newCounter(), IOContext.READ, dvType)
      case FIXED_INTS_16 => new MutableDVConsumer(dir, fieldId, Counter.newCounter(), IOContext.READ, dvType)
      case FIXED_INTS_32 => new MutableDVConsumer(dir, fieldId, Counter.newCounter(), IOContext.READ, dvType)
      case FIXED_INTS_64 => new MutableDVConsumer(dir, fieldId, Counter.newCounter(), IOContext.READ, dvType)
      case _             => throw new IllegalArgumentException("Not supported doc values type: %s".format(dvType))
    }
  }

  /**
   * Instantiates a new doc values producer
   * @param dir provides access to a flat list of files
   * @param fieldId a unique indefiner for a given field
   * @param dvType a data type for a given doc value field
   * @return a new doc values producer.
   **/
  protected def getDocValues(dir: Directory, fieldId: String, dvType: DocValues.Type): DocValues = {
    import DocValues.Type._
    return dvType match {
      case FIXED_INTS_8  => new MutableDVReader(dir, fieldId, Counter.newCounter(), 0, IOContext.READ, dvType)
      case FIXED_INTS_16 => new MutableDVReader(dir, fieldId, Counter.newCounter(), 0, IOContext.READ, dvType)
      case FIXED_INTS_32 => new MutableDVReader(dir, fieldId, Counter.newCounter(), 0, IOContext.READ, dvType)
      case FIXED_INTS_64 => new MutableDVReader(dir, fieldId, Counter.newCounter(), 0, IOContext.READ, dvType)
      case _             => throw new IllegalArgumentException("This doc values type: %s is not supported.".format(dvType))
    }
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
  }


}
