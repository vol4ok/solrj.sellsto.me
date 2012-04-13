package sellstome.lucene.codecs.values

import org.apache.lucene.index.DocValues.{Type, Source}
import org.apache.lucene.store.IndexInput

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
class PackedArraySource(dvType: Type, dataInputs: List[IndexInput]) extends Source(dvType) {



}