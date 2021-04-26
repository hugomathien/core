package javapipelines;

import java.time.Instant;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.lit;

public class DummyPipeline implements IDatasetTransformer {

	@Override
	public Dataset<Row> compute(SparkSession sparkSession, Dataset<Row> df, Instant t) {	
		df = df.withColumn("testCol",lit("HELLO_WORLD"));	
		df.show();
		return df;
	}

}
