package config;

import java.util.List;
import java.util.function.Function;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import streaming.source.MemoryStreamWrapper;
import streaming.source.StreamQueryWrapper;
import utils.Spark;

@Configuration
public class SparkConfig {

	@Bean
	@Lazy
	public Spark spark() {
		Spark spark = new Spark();
		return spark;
	}

	@Bean
	@Lazy
	public SparkSession sparkSession(Spark spark) {
		SparkSession sparkSession = spark.sparkSession();
		return sparkSession;
	}

	// TODO We are allowing for only one memory stream created programmatically ?
	@Bean
	@Lazy
	@Scope(value = "prototype")
	MemoryStreamWrapper getMemoryStreamWrapper() {
		return new MemoryStreamWrapper();
	}
	
	@Bean
	@Lazy
	@Scope(value = "prototype")
	StreamQueryWrapper getStreamQueryWrapper(MemoryStreamWrapper memoryStreamWrapper) {
		return new StreamQueryWrapper(memoryStreamWrapper);
	}
	
	@Bean
	@Lazy
	@Scope(value = "prototype")
	StreamQueryWrapper getStreamQueryWrapper(MemoryStreamWrapper memoryStreamWrapper,String outputMode,String format,String queryName,String checkpointLocation,String path,List<Function<Dataset<Row>,Dataset<Row>>> transforms,String... partitionColumns) {
		return new StreamQueryWrapper(memoryStreamWrapper,outputMode,format,queryName,checkpointLocation,path,transforms,partitionColumns);
	}


}
