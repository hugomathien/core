package pipeline;

import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import javapipelines.IDatasetTransformer;

import org.apache.spark.sql.streaming.DataStreamReader;

import java.time.Instant;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import scalapipelines.MLPipeline1;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class StaticMLPipeline {
	@Autowired
	private SparkSession sparkSession;
	private Dataset<Row> df;
	private IDatasetTransformer pipeline;
	
	
	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		Logger.getRootLogger().setLevel(Level.OFF);

		// prepare a dataframe
		df = sparkSession
				.read()
				.parquet("C:\\Users\\suyux\\marketdata\\bloomberg\\stock\\datasetNoDupes");		
		pipeline = new MLPipeline1();
	}
	
	@Test
	public void runpipeline() {
		pipeline.compute(sparkSession, df, Instant.now());
	}
	
}
