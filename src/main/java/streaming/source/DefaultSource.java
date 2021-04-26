package streaming.source;

import java.util.Map;

import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableProvider;
import org.apache.spark.sql.connector.expressions.Transform;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
/*
 * Default source should some kind of relation provider
 */

public class DefaultSource implements TableProvider {

	public DefaultSource() {
		
	}
	
	@Override
	public StructType inferSchema(CaseInsensitiveStringMap options) {
		return getTable(null,new Transform[]{},options.asCaseSensitiveMap()).schema();

	}

	@Override
	public Table getTable(StructType schema, Transform[] partitioning, Map<String, String> properties) {
		return new SimpleStreamingTable();
	}
}