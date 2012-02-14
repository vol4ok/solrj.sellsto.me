package sellstome.solr.response.json.converters

import collection.Map
import sellstome.solr.schema.MoneyType
import org.apache.solr.schema._

/**
 * A registry. Here we can find a corresponding converter for a given solr field type.
 * @author Alexander Zhugrov
 * @since 1.0
 */
object FieldConverters extends Map[Class[_ <: FieldType], SolrField2JsonConverter] {

  val delegate = Map[Class[_ <: FieldType], SolrField2JsonConverter](
    classOf[StrField] -> StringSolrFieldConverter,
    classOf[TextField] -> StringSolrFieldConverter,
    classOf[MoneyType] -> MoneySolrFieldConverter,
    classOf[TrieLongField] -> StringSolrFieldConverter,
    classOf[LatLonType] -> LocationSolrFieldConverter,
    classOf[BoolField] -> StringSolrFieldConverter)

  def get(key: Class[_ <: FieldType]) = delegate.get(key)

  def iterator = delegate.iterator

  def +[B >: SolrField2JsonConverter](kv: (Class[_ <: FieldType], B)) =
    throw new UnsupportedOperationException("We don't have any reasons to implement this method here")

  def -(key: Class[_ <: FieldType]) =
    throw new UnsupportedOperationException("We don't have any reasons to implement this method here")
}
