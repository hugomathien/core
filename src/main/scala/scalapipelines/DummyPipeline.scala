package scalapipelines
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import java.time.Instant
import javapipelines.IDatasetTransformer;

class DummyPipeline extends IDatasetTransformer {
	def compute(spark: SparkSession,df: Dataset[Row], t: Instant): Dataset[Row] = { //TODO: add instant parameter
				val newdf = df.withColumn("testCol", lit("HELLO_WORLD"))
				newdf.show(false)
				newdf
	}
}
