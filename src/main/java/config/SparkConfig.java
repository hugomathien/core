package config;

import java.util.List;
import java.util.function.Function;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

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

}
