package utils;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class Spark {
	@Value("${spark.appname}")
	private String appName;
	@Value("${spark.master}")
	private String master;
	@Value("${spark.serializer}")
	private String serializer;
	@Value("${spark.executor.memory}")
	private String executorMemory;
	@Value("${spark.driver.memory}")
	private String driverMemory;
	@Value("${spark.driver.cores}")
	private String driverCores;
	@Value("${spark.executor.cores}")
	private String executorCores;
	@Value("${spark.local.dir}")
	private String localDir;
	@Value("${spark.java.io.tmp.dir}")
	private String ioTmpDir;
	@Value("${spark.streaming.millis.duration}")
	private Integer streamingMillisDuration;
	@Value("${spark.testing.memory}")
	private Long testingMemory;
	
	
	public Spark() {
		
	}
	
	public SparkSession sparkSession() {
		return SparkSession
				  .builder()
				  .appName(appName)
				  .config("spark.master", master)
				  .config("spark.serializer", serializer)
				  .config("spark.executor.memory", executorMemory)
				  .config("spark.driver.memory", driverMemory)
				  .config("spark.driver.cores", driverCores)
				  .config("spark.executor.cores", executorCores)
				  .config("spark.streaming.millis.duration", streamingMillisDuration)
				  .config("spark.testing.memory", testingMemory)
				  .getOrCreate();
	}
	
	public JavaStreamingContext sparkStreaming() {
		SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("Spark Streaming");
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(10));
		
		return jssc; // TODO Spark Streaming init
	}
}
