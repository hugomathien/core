package utils;

import org.apache.spark.sql.execution.datasources.v2.FileDataSourceV2;

public class CSVStreamingSource implements FileDataSourceV2, MicroBatchReadSupport {

    public CSVStreamingSource() {

    }

    @Override
    public CSVMicroBatchReader createMicroBatchReader(Optional<StructType> schema, String checkpointLocation, DataSourceOptions options) {
        return new CSVMicroBatchReader(options.get("filepath").get());
    }
}
