package streaming.source;

import org.apache.spark.sql.connector.read.InputPartition;
import org.apache.spark.sql.connector.read.PartitionReaderFactory;
import org.apache.spark.sql.connector.read.streaming.MicroBatchStream;
import org.apache.spark.sql.connector.read.streaming.Offset;

public class SimpleMicroBatchStream implements MicroBatchStream {
	private int latestOffsetValue;

	public SimpleMicroBatchStream() {
		
	}
	
	@Override
	public Offset initialOffset() {
		return new SimpleOffset(latestOffsetValue);
	}

	@Override
	public Offset deserializeOffset(String json) {
		return new SimpleOffset(latestOffsetValue);
	}

	@Override
	public void commit(Offset end) {
		return;
	}

	@Override
	public void stop() {
		return;
	}

	@Override
	public Offset latestOffset() {
		this.latestOffsetValue += 10;
		return new SimpleOffset(this.latestOffsetValue);

	}

	@Override
	public InputPartition[] planInputPartitions(Offset start, Offset end) {
		return new InputPartition[] {new SimplePartition()};
	}

	@Override
	public PartitionReaderFactory createReaderFactory() {
		return new SimplePartitionReaderFactory();
	}

}