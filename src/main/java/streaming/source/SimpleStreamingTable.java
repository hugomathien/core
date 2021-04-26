package streaming.source;

import java.util.HashSet;
import java.util.Set;

import org.apache.spark.sql.connector.catalog.SupportsRead;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCapability;
import org.apache.spark.sql.connector.read.ScanBuilder;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;

import org.apache.spark.sql.util.CaseInsensitiveStringMap;

public class SimpleStreamingTable implements Table,SupportsRead {
	
	
	@Override
	public ScanBuilder newScanBuilder(CaseInsensitiveStringMap options) {
		return new SimpleScanBuilder();
	}

	@Override
	public String name() {
		return this.getClass().toString();
	}

	@Override
	public StructType schema() {
		StructField[] structFields = new StructField[]{
				new StructField("Value", DataTypes.StringType, true, Metadata.empty())	     
		};
		
		return  new StructType(structFields);
	}

	@Override
	public Set<TableCapability> capabilities() {
		HashSet<TableCapability> set = new HashSet<TableCapability>();
		set.add(TableCapability.MICRO_BATCH_READ);
		return set;
	}
}
