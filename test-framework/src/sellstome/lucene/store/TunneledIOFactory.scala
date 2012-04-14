package sellstome.lucene.store

import org.apache.lucene.store.{IndexInput, IndexOutput}
import gnu.trove.list.array.TByteArrayList
import gnu.trove.list.TByteList


/**
 * A factory class for creating a tunneled IO
 * where reader can read everything that was written to a writer
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class TunneledIOFactory {

 /** Creates a new in/out pair where everything that written to out could be read from in */
 def newPair(): (IndexOutput, IndexInput) = {
   val out = new TunneledIndexOutput()
   return (out, new TunneledIndexInput(out))
 }

 protected class TunneledIndexOutput extends IndexOutput {
   /** the underlying data storage */
   protected val _bytes: TByteList = new TByteArrayList()

   def flush() { ??? }

   def close() { ??? }

   def getFilePointer = ???

   def seek(pos: Long) { ??? }

   def length() = _bytes.size

   def writeByte(b: Byte) {
    _bytes.add(b)
   }

   def writeBytes(b: Array[Byte], offset: Int, length: Int) {
     _bytes.add(b, offset, length)
   }

   /** gets underlying raw data */
   def rawData: TByteList = {
     return _bytes
   }

 }

 protected class TunneledIndexInput(output: TunneledIndexOutput) extends IndexInput("A tunneled IndexInput") {
   /** the underlying data storage */
   protected val _bytes:TByteList = output.rawData
   /** the position of the element that will be read  */
   var _pos:Int = 0

   def readByte(): Byte = {
    _pos += 1
    if (_pos > _bytes.size)
      throw new IndexOutOfBoundsException(s"trying to read byte at pos: ${_pos-1} while underlying data size: ${_bytes.size}")
    return _bytes.get(_pos - 1)
   }

   def readBytes(b: Array[Byte], offset: Int, len: Int) {
    try {
      _bytes.toArray(b, _pos, offset, len)
    }
    catch {
      case e: Throwable => {
        Console.println(s"offset: $offset")
        Console.println(s"len: $len")
        Console.println(s"_pos: ${_pos}")
        Console.println(s"_bytes.size: ${_bytes.size}")
        throw e
      }
    }
    _pos += len
   }

   def close() { ??? }

   def getFilePointer = ???

   def seek(pos: Long) { ??? }

   def length() = _bytes.size

 }

}