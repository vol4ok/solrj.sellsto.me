package sellstome.lucene.io.packed.array

/**
 * A generic reader for sparse array format.
 * A sparse array format compression could be represented as following
 * The input array should be presorted in asc order. The presorted array could
 * contain a duplicates. We write only the actual values to the output stream. Also we write gaps in
 * ords between adjusted values.
 * Each block contains a 8 slots.
 * Each block slot has size specific for a given datatype
 * @see [[sellstome.lucene.io.packed.array.Type.size]]
 * Each block starts with one byte value (<b>Descriptor</b>) where each bit describes all slots for a given block
 * If a bit at position <i>i</i> in a Descriptor is set that means that slot at position <i>i</i> encodes a value for a given block
 * If a bit at position <i>i</i> in a Descriptor is unset that means that slot at position <i>i</i> encodes a gap for ords at adjusted positions
 * In case if the gap for ords between adjusted positions is equal to 1 we ommit the gap value and simply write the next value
 * If the ord the first element is not a 0 we write a gap first relative to the -1 ord value
 * In case if we write gap as the last value for a given block then the first value in the next block would be the adjusted
 * value for a given block in this sense the blocks depends on each other.
 * note: this version should support initialization from multiple sources
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class PackedArrayReader[V](dataType: Type[V]) {

}