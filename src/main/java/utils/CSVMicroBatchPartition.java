package utils;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.read.InputPartition;
import org.apache.spark.sql.connector.read.PartitionReader;
import org.apache.spark.sql.connector.read.PartitionReaderFactory;

import scala.collection.JavaConversions;

class CSVMicroBatchPartition implements InputPartition {

    protected List<Object[]> rowChunk;

    CSVMicroBatchPartition(List<Object[]> rowChunk) {

        this.rowChunk = rowChunk;
    }

  
}

class CSVMicroBatchPartitionReaderFactory implements PartitionReaderFactory {


    CSVMicroBatchPartitionReaderFactory() {
    }

	@Override
	public PartitionReader<InternalRow> createReader(InputPartition partition) {
        return new CSVMicroBatchPartitionReader(partition);

	}
}

class CSVMicroBatchPartitionReader implements PartitionReader<InternalRow> {

    private List<Object[]> rowChunk;
    private int counter;

    CSVMicroBatchPartitionReader(InputPartition part) {     
    	this.rowChunk = ((CSVMicroBatchPartition) part).rowChunk;
        this.counter = 0;
    }

    @Override
    public boolean next() {
        return counter < rowChunk.size();
    }

    @Override
    public InternalRow get() {

        Object[] row = rowChunk.get(counter++);

        return InternalRow.apply(JavaConversions.asScalaBuffer(Arrays.asList(row)).seq());
    }

    @Override
    public void close() {

    }
}