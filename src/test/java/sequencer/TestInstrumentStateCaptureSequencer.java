package sequencer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;

import event.sequencing.DatasetPipelineSequencer;
import event.sequencing.InstrumentStateCaptureSequencer;
import event.sequencing.StreamQuerySequencer;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;

import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import javapipelines.DummyPipeline;
import org.apache.spark.sql.expressions.*;
import static org.apache.spark.sql.functions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestInstrumentStateCaptureSequencer {
	private InstrumentStateCaptureSequencer sequencerCapture;
	private StreamQuerySequencer sequencerQuery;
	private DatasetPipelineSequencer sequencerPipeline;
	@Autowired
	private SparkSession sparkSession;


	@Before
	public void setup() throws DataQueryException, DataServiceStartException {

		DataRequest<?> request = new DataRequest.Builder<>()
				.dataService(DataServiceEnum.RANDOMGEN)
				.backfill(false)
				.fields("PX_LAST","VOLUME")
				.parameters(RequestParameters.startDate, LocalDate.of(2021,3,15))
				.parameters(RequestParameters.endDate, LocalDate.of(2021, 3, 31))
				.parameters(RequestParameters.randomizedNumberScales,new double[] {1.0,100.0})
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.SingleStock, new String[]{"FP FP","VOD LN"})
				.requestType(RequestType.HistoricalDataRequest)
				.build();

		request.query();

	}

	@Test
	public void testInstrumentStateSequencer() throws TimeoutException  {	
		Logger.getRootLogger().setLevel(Level.OFF);
		List<Function<Dataset<Row>,Dataset<Row>>> streamQueryTransforms = new ArrayList<Function<Dataset<Row>,Dataset<Row>>>();
		streamQueryTransforms.add(func);

		sparkSession.sparkContext().setCheckpointDir("C:\\Users\\suyux\\tmp");

		sequencerCapture = (InstrumentStateCaptureSequencer) new InstrumentStateCaptureSequencer.Builder()
				.spotDataFields("PX_LAST","VOLUME")
				.instrumentType(InstrumentType.SingleStock)
				.identifierTypes(IdentifierType.valueOf("TICKER"))
				.identifiers("FP FP","VOD LN")								
				.startDate(LocalDate.of(2021, 3, 15))
				.endDate(LocalDate.of(2021, 3, 31))
				.step(Duration.ofDays(1))		
				.priority(0)
				.build();

		sequencerQuery = (StreamQuerySequencer) new StreamQuerySequencer.Builder()
				.transforms(streamQueryTransforms)
				.memoryStream(sequencerCapture.getMemoryStream())
				.format("memory")
				.outputMode("complete")				
				.queryName("myStreamQuery")
				.startDate(LocalDate.of(2021, 3, 15))
				.endDate(LocalDate.of(2021, 3, 31))
				.step(Duration.ofDays(1))
				.priority(1)
				.build();
		
		//sequencerQuery.getQuery().setPrint(true);
		
		DummyPipeline pipeline = new DummyPipeline();
		
		sequencerPipeline = (DatasetPipelineSequencer) new DatasetPipelineSequencer.Builder()
		.transformer(pipeline)
		.entryTableName("myStreamQuery")
		.exitTableName("myTransformerDf")
		.startDate(LocalDate.of(2021, 3, 15))
		.endDate(LocalDate.of(2021, 3, 31))
		.step(Duration.ofDays(1))
		.priority(2)
		.build();

		CoreConfig.services().run();
	}

	public static Function<Dataset<Row>,Dataset<Row>> func = new Function<Dataset<Row>,Dataset<Row>>() {
		@Override
		public Dataset<Row> apply(Dataset<Row> df) {
			return  df.withWatermark("datetime", "3 days")
					.groupBy(
							df.col("datetime"),
							functions.window(df.col("datetime"), "10 days","1 day"),
							df.col("ticker"))
					.count()
					.withColumnRenamed("window", "rollingWindow")
					.withColumn("windowEnd", functions.expr("rollingWindow.end"))
					.withColumn("windowEndDate", functions.expr("to_date(windowEnd)"));
		}

	};


	public static Function<Dataset<Row>,Dataset<Row>> leadPrice = new Function<Dataset<Row>,Dataset<Row>>() {
		@Override
		public Dataset<Row> apply(Dataset<Row> df) {
			df = df.withColumn("date", to_date(col("datetime")));
			WindowSpec dt_window = Window
					.partitionBy("ticker")
					.orderBy("date")
					.rowsBetween(Window.currentRow(), 3);
			return  df.withColumn("leadPrice", last(df.col("PX_LAST"),true).over(dt_window));
		}

	};
	

	
}
