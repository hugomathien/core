package streaming.source;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.read.InputPartition;
import org.apache.spark.sql.connector.read.PartitionReader;
import org.apache.spark.sql.connector.read.PartitionReaderFactory;

public class SimplePartitionReaderFactory implements PartitionReaderFactory {
	
	public SimplePartitionReaderFactory() {
		
	}
	
	@Override
	public PartitionReader<InternalRow> createReader(InputPartition partition) {
		return new SimplePartitionReader();
	}
}
