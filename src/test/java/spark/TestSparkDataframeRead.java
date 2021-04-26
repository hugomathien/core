package spark;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.connector.read.streaming.Offset;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.Column;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import event.processing.EventPriorityQueue;
import finance.misc.Exchange;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import org.apache.spark.sql.Column;
import streaming.source.MemoryStreamWrapper;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.execution.streaming.MemoryStream;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.expressions.Window;

import org.apache.spark.sql.Encoders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestSparkDataframeRead {


	@Autowired
	private SparkSession sparkSession;

	@Autowired 
	private EventPriorityQueue queue;
	private String[] partitionColumns = new String[] {"year"};
	
	@Test
	public void testSparkSession() throws TimeoutException  {	
		readSourceAsDataframe(sparkSession);
	}

	private void readSourceAsDataframe(SparkSession sparkSession) throws TimeoutException {
		Dataset<Row> df = sparkSession
				.read()
				.parquet("C:\\Users\\suyux\\marketdata\\bloomberg\\stock\\datasetNoDupes");		
		df.filter("ticker='VOD LN' and year=2021").orderBy("date").show(1000);
		//System.out.println(df.count());
		//df.filter("date = '2021-04-19'").show(1000000,false);
	}
}
