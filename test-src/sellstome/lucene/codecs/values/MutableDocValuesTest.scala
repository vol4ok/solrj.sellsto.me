package sellstome.lucene.codecs.values

import sellstome.lucene.util.SellstomeLuceneTestCase
import org.apache.lucene.codecs.DocValuesConsumer
import org.apache.lucene.util.{Counter, BytesRef}
import java.io.Reader
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import org.apache.lucene.index.DocValues.Type
import org.apache.lucene.store.{FlushInfo, MergeInfo, IOContext, Directory}
import java.util.Random
import org.apache.lucene.index._

/**
 * todo zhugrov a - classify a type for this test
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class MutableDocValuesTest extends SellstomeLuceneTestCase {

  def testFixedInts() {
    testInts(Type.FIXED_INTS_64, 63)
    testInts(Type.FIXED_INTS_32, 31)
    testInts(Type.FIXED_INTS_16, 15)
    testInts(Type.FIXED_INTS_8, 7)
  }

  /** Tests the merge functionality */
  def testMerge() {
    //how to determine to which segment is written
    ???
  }

  protected def testMergeFor[T]() (implicit arrTag: ArrayTag[T], erTag: ErasureTag[T]) {
    val numMerge = numGen.nextIntInRange(3, 10)
    val dir: Directory = newDirectory()
    for (i <- 0 until numMerge) {
      val writer = getDocValuesConsumer(dir, s"test$i", docTypeOf[T])
      val numDoc = numGen.nextIntInRange(100, 10000)
      for (j <- 0 until numDoc) {
        writer.add(j, new DocValueHolder(numberOf[T](numGen.nextNumber[T])))
      }
    }
  }

  //region Utils
  protected def testInts(docType: DocValues.Type, maxBit: Int)() {
    var maxV: Long = 1
    val DocCount: Int = 333 + random.nextInt(333)
    val values = new Array[Long](DocCount)

    for (rx <- 1 until maxBit) {
      val dir: Directory = newDirectory()
      val writer: DocValuesConsumer = getDocValuesConsumer(dir, "test", docType)
      for (docIdWrite <- 0 until DocCount) {
        val value: Long = random.nextLong % (1 + maxV)
        values(docIdWrite) = value
        writer.add(docIdWrite, new DocValueHolder(value))
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
    val dir: Directory = newDirectory()
    val w: DocValuesConsumer = getDocValuesConsumer(dir, "test", docType)
    val NUM_VALUES: Int = 777 + random.nextInt(777)
    val values: Array[Double] = new Array[Double](NUM_VALUES)
    for (i <- 0 until NUM_VALUES) {
      val v: Double = if (docType eq Type.FLOAT_32) random.nextFloat else random.nextDouble
      values(i) = v
      w.add(i, new DocValueHolder(v))
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

  protected def docTypeOf[T](implicit erasureTag: ErasureTag[T]): Type = {
    if (erasureTag.getClass == classOf[Byte]) {
      return Type.FIXED_INTS_8
    } else if (erasureTag.getClass == classOf[Short]) {
      return Type.FIXED_INTS_16
    } else if (erasureTag.getClass == classOf[Int]) {
      return Type.FIXED_INTS_32
    } else if (erasureTag.getClass == classOf[Long]) {
      return Type.FIXED_INTS_64
    } else if (erasureTag.getClass == classOf[Float]) {
      return Type.FLOAT_32
    } else if (erasureTag.getClass == classOf[Double]) {
      return Type.FLOAT_64
    } else {
      throw new IllegalArgumentException(s"Not supported erasure type: ${erasureTag.erasure}")
    }
  }

  protected def numberOf[T](of: T)(implicit erasureTag: ErasureTag[T]): Number = {
    if (erasureTag.getClass == classOf[Byte]) {
      return of.asInstanceOf[Byte]
    } else if (erasureTag.getClass == classOf[Short]) {
      return of.asInstanceOf[Short]
    } else if (erasureTag.getClass == classOf[Int]) {
      return of.asInstanceOf[Int]
    } else if (erasureTag.getClass == classOf[Long]) {
      return of.asInstanceOf[Long]
    } else if (erasureTag.getClass == classOf[Float]) {
      return of.asInstanceOf[Float]
    } else if (erasureTag.getClass == classOf[Double]) {
      return of.asInstanceOf[Double]
    } else {
      throw new IllegalArgumentException(s"Not supported erasure type: ${erasureTag.erasure}")
    }
  }

  /**
   * Instantiates a new doc values consumer
   * @param dir provides access to a flat list of files
   * @param fieldId a unique identifier for DV field. This is combination of a segment name and field identifier.
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
      case FIXED_INTS_8  => new MutableDVReader(dir, fieldId, 0, IOContext.READ, dvType)
      case FIXED_INTS_16 => new MutableDVReader(dir, fieldId, 0, IOContext.READ, dvType)
      case FIXED_INTS_32 => new MutableDVReader(dir, fieldId, 0, IOContext.READ, dvType)
      case FIXED_INTS_64 => new MutableDVReader(dir, fieldId, 0, IOContext.READ, dvType)
      case _             => throw new IllegalArgumentException("This doc values type: %s is not supported.".format(dvType))
    }
  }
  //endregion

  class DocValueHolder(_numberValue: Number) extends IndexableField {
    def numericValue: Number = _numberValue
    def tokenStream(a: Analyzer): TokenStream = ???
    def boost: Float = ???
    def name: String = ???
    def binaryValue: BytesRef = ???
    def stringValue: String = ???
    def readerValue: Reader = ???
    def fieldType: IndexableFieldType = ???
  }

}
