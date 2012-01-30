package sellstome.search.solr.schema.finance

import org.apache.solr.schema.SchemaField
import org.apache.lucene.queries.function.ValueSource
import java.util.List
import org.apache.lucene.queries.function.valuesource.VectorValueSource

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 24.01.12
 * Time: 11:31
 * Instantiates {@link FunctionValues} for a particular reader.
 */
private[schema] class MoneyValueSource(sf: SchemaField, sources: List[ValueSource]) extends VectorValueSource(sources) {

  override def name = "money"
  override def description = name() + "(" + sf.getName + ");"

}