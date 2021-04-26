package streaming.source;

import org.apache.spark.sql.connector.read.Scan;
import org.apache.spark.sql.connector.read.streaming.MicroBatchStream;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class SimpleScan implements Scan{

	public SimpleScan() {
		
	}
	
	@Override
	public StructType readSchema() {
		StructField[] structFields = new StructField[]{
				new StructField("Value", DataTypes.StringType, true, Metadata.empty())	     
		};
		
		return  new StructType(structFields);
	}

	@Override
	public MicroBatchStream toMicroBatchStream(String checkpoint) {
		return new SimpleMicroBatchStream();
	}

}