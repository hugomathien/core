package javapipelines;

import java.time.Instant;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Row;

public interface IDatasetTransformer {
	
public Dataset<Row> compute(SparkSession sparkSession,Dataset<Row> df,Instant t);


}
