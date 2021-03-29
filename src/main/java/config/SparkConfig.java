package config;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;

import utils.Spark;

@Configuration
@PropertySource("classpath:/config/spark.properties")
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
	
	
}
