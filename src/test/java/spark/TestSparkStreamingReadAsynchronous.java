package spark;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.connector.read.streaming.Offset;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import event.processing.EventPriorityQueue;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.execution.streaming.MemoryStream;
import org.apache.spark.sql.Encoders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestSparkStreamingReadAsynchronous {


	@Autowired
	private SparkSession sparkSession;
	@Autowired 
	private EventPriorityQueue queue;
	private static int dayOffset = 300;
	static {

		String OS = System.getProperty("os.name").toLowerCase();

		if (OS.contains("win")) {
			System.setProperty("hadoop.home.dir", Paths.get("C:\\Users\\suyux\\hadoop-3.2.0").toAbsolutePath().toString());
			System.setProperty("hadoop.log.dir", "C:\\Users\\suyux\\tmp");

		} else {
			System.setProperty("hadoop.home.dir", "/");
		}

	}

	@Test
	public void testSparkSession() throws TimeoutException, InterruptedException  {
		//System.setProperty("hadoop.home.dir", "/");

		Logger.getRootLogger().setLevel(Level.OFF);

		writeSourceAsStream(sparkSession);
	}

	private void writeSourceAsStream(SparkSession sparkSession) throws TimeoutException, InterruptedException {
		StructField[] structFields = new StructField[]{
				new StructField("Date", DataTypes.TimestampType, true, Metadata.empty()),
				new StructField("Name", DataTypes.StringType, true, Metadata.empty()),
				new StructField("Price", DataTypes.DoubleType, true, Metadata.empty()),
				new StructField("Volume", DataTypes.IntegerType, true, Metadata.empty())     
		};

		StructType schema = new StructType(structFields);
		ExpressionEncoder encoder = RowEncoder.apply(schema);
		MemoryStream<Object> testStream = new MemoryStream<Object>(1, sparkSession.sqlContext(), null, encoder);

		System.out.println(testStream.fullSchema().toString());

		sparkSession.sparkContext().setCheckpointDir("C:\\Users\\suyux\\tmp");

		Dataset<Row> dataset = testStream.toDF().as(encoder);
		/*
		 * dataset = dataset .withWatermark("Date", "3 days") .groupBy(
		 * functions.window(dataset.col("Date"), "10 days","1 day"),
		 * dataset.col("Name")) .count() .withColumnRenamed("window", "rollingWindow")
		 * .withColumn("windowEnd", functions.expr("rollingWindow.end"))
		 * .withColumn("windowEndDate", functions.expr("to_date(windowEnd)"));
		 */

		DataStreamWriter<Row> streamWriter = dataset
				.writeStream()
				.outputMode("append")
				.format("parquet")
				.option("checkpointLocation", "C:\\Users\\suyux\\data")
				.option("path", "C:\\Users\\suyux\\data")			
				.queryName("testQuery");

		StreamingQuery query = streamWriter.start();		   

		Thread thread = new Thread(){
			public void run(){
				try {
					System.out.println("Stream query running");
					query.awaitTermination();
				} catch (StreamingQueryException e) {
					e.printStackTrace();
				}
			}
		};

		thread.start();

		System.out.println("Stream publisher Running");
		while(true){
			java.sql.Date dt = java.sql.Date.valueOf(LocalDate.now().minusDays(dayOffset));
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			java.sql.Timestamp timestamp = new java.sql.Timestamp(dt.getTime());
			String timestampString = timestamp.toString();

			System.out.println(dt.toString());
			Offset offset = null;
			for(int i=1;i<=1;i++) {

				Row row1 = RowFactory.create(timestamp,"FP FP",33.53,1000);
				Row row2 = RowFactory.create(timestamp,"VOD LN",2343.09,2000);
				Row row3 = RowFactory.create(timestamp,"AI FP",13.48,3000);

				List<Object> testList = Arrays.asList(row1,row2,row3);					
				Seq<Object> seq = JavaConverters.asScalaIteratorConverter(testList.iterator()).asScala().toSeq();										
				offset = testStream.addData(seq);
			}
			dayOffset--;
		}



	}
}
