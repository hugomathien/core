package pipeline;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import event.sequencing.DataRequestSequencer;
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
import marketdata.services.bloomberg.enumeration.RequestOverrides;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class BloombergMarketDataWriter {
	private InstrumentStateCaptureSequencer sequencerCapture;
	private StreamQuerySequencer sequencerQuery;
	@Autowired
	private SparkSession sparkSession;
	private String[] identifiers = {};
	private String[] universe = {"SXXP"};
	private String[] fields = {"PX_LAST",
							   "OPEN",
							   "HIGH",
							   "LOW",
							   "EQY_WEIGHTED_AVG_PX",
							   "VOLUME",
							   "VOLATILITY_30D",
							   "VOLATILITY_60D",
							   "NEWS_SENTIMENT_DAILY_AVG",
							   "PUT_CALL_VOLUME_RATIO_CUR_DAY",
							   "30DAY_IMPVOL_100.0%MNY_DF",
							   "30DAY_IMPVOL_90.0%MNY_DF",
							   "30DAY_IMPVOL_110.0%MNY_DF",
							   "60DAY_IMPVOL_100.0%MNY_DF",
							   "60DAY_IMPVOL_90.0%MNY_DF",
							   "60DAY_IMPVOL_110.0%MNY_DF",
							   "TOT_ANALYST_REC",
							   "TOT_BUY_REC",
							   "TOT_SELL_REC",
							   "TOT_HOLD_REC",
							   "PCT_INSIDER_SHARES_OUT",
							   "EQY_SH_OUT",
							   "MF_BLCK_1D",
							   "MF_NONBLCK_1D"};
	
	private String[] fieldWithOverrides = {
			"BEST_EPS_MEDIAN_1BF",
			"BEST_EPS_MEDIAN_2BF",
			"BEST_EPS_MEDIAN_3BF",
			"BETA_ADJ_1M",
			"BETA_ADJ_3M",
			"ALPHA_ADJ_1M",
			"ALPHA_ADJ_3M",
			"BEST_TARGET_PRICE_MEDIAN"};
	
	private LocalDate universeStartDate = LocalDate.of(2002, 1, 2);
	private LocalDate startDate = LocalDate.of(2013, 1, 3);
	private LocalDate endDate = LocalDate.of(2021, 4, 19);
	private Duration requestStep = Duration.parse("P365D");
	private Duration requestLookForward = Duration.parse("P365D");
	private String exportPath = "C:\\Users\\suyux\\marketdata\\bloomberg\\stock\\data2";
	private String checkpointPath = "C:\\Users\\suyux\\marketdata\\bloomberg\\stock\\data2";
	private String[] partitionColumns = {"date"};
	
	
	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
	
		this.dataQueryUniverseComposition();
		this.dataQueryWithoutOverrides();
		this.dataQueryWithOverrides();
	}
	
	private void dataQueryUniverseComposition () {
		// 1 - universe composition query
		DataRequestSequencer<Object> idxrequest = (DataRequestSequencer<Object>) new DataRequestSequencer.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields("INDX_MWEIGHT_HIST")
		.override(RequestOverrides.END_DATE_OVERRIDE, startDate)
		.identifierType(IdentifierType.TICKER)
		.instrumentType(InstrumentType.Index)
		.identifiers(universe)
		.requestType(RequestType.ReferenceDataRequest)
		.startDate(universeStartDate)
		.endDate(endDate)
		.step(requestStep)
		.priority(-3)
		.build();
			
	}
	
	private void dataQueryWithoutOverrides () {
		DataRequestSequencer<Object> request = (DataRequestSequencer<Object>) new DataRequestSequencer.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields(fields)
		//.parameters(RequestParameters.randomizedNumberScales,new double[] {100.0,10000.0})
		.parameters(RequestParameters.nonTradingDayFillOption, "NON_TRADING_WEEKDAYS")
		.parameters(RequestParameters.nonTradingDayFillMethod, "PREVIOUS_VALUE")
		.parameters(RequestParameters.adjustmentAbnormal, true)
		.parameters(RequestParameters.adjustmentNormal, true)
		.parameters(RequestParameters.adjustmentSplit, true)
		.identifierType(IdentifierType.TICKER)
		.instrumentType(InstrumentType.SingleStock)
		.universe(universe)
		.identifiers(identifiers)
		.requestType(RequestType.HistoricalDataRequest)
		.startDate(startDate)
		.endDate(endDate)
		.windowLookForward(requestLookForward)
		.step(requestStep)
		.priority(-2)
		.build();	
		
	}
	
	private void dataQueryWithOverrides () {
		
		ArrayList<DataRequestSequencer.Builder<Object>> requestBuilders = new ArrayList<DataRequestSequencer.Builder<Object>>();
		
		requestBuilders.add(new DataRequestSequencer.Builder<Object>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("BEST_EPS_MEDIAN_1BF")
				.parameters(RequestParameters.adjustmentAbnormal, true)
				.parameters(RequestParameters.adjustmentNormal, true)
				.parameters(RequestParameters.adjustmentSplit, true)
				.override(RequestOverrides.BEST_FPERIOD_OVERRIDE, "1BF"));
		
		// EPS MEDIAN 2BF
		requestBuilders.add(new DataRequestSequencer.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("BEST_EPS_MEDIAN_2BF")
				.override(RequestOverrides.BEST_FPERIOD_OVERRIDE, "2BF"));	
		
		// EPS MEDIAN 3BF
		requestBuilders.add(new DataRequestSequencer.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("BEST_EPS_MEDIAN_3BF")
				.override(RequestOverrides.BEST_FPERIOD_OVERRIDE, "3BF"));			
		
		// BETA ADJ 1M
		requestBuilders.add(new DataRequestSequencer.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("BETA_ADJ_1M")
				.override(RequestOverrides.BETA_CALC_INTERVAL_OVERRIDE, "1M")
				.override(RequestOverrides.BETA_OVERRIDE_PERIOD, "D"));
		
		// BETA ADJ 3M
		requestBuilders.add(new DataRequestSequencer.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("BETA_ADJ_3M")
				.override(RequestOverrides.BETA_CALC_INTERVAL_OVERRIDE, "3M")
				.override(RequestOverrides.BETA_OVERRIDE_PERIOD, "D"));	
		
		// ALPHA ADJ 1M
		requestBuilders.add(new DataRequestSequencer.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("ALPHA_ADJ_1M")
				.override(RequestOverrides.BETA_CALC_INTERVAL_OVERRIDE, "1M")
				.override(RequestOverrides.BETA_OVERRIDE_PERIOD, "D"));
		
		// ALPHA ADJ 3M
		requestBuilders.add(new DataRequestSequencer.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("ALPHA_ADJ_3M")
				.override(RequestOverrides.BETA_CALC_INTERVAL_OVERRIDE, "3M")
				.override(RequestOverrides.BETA_OVERRIDE_PERIOD, "D"));
		
		// TARGET PRICE MEDIAN
		requestBuilders.add(new DataRequestSequencer.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("BEST_TARGET_PRICE_MEDIAN")
				.override(RequestOverrides.BEST_CONSENSUS_STAT_OVERRIDE, "median"));
		
		
		// Build the requests
		for (DataRequestSequencer.Builder<Object> requestBuilder : requestBuilders) {
			requestBuilder.identifierType(IdentifierType.TICKER)
			.instrumentType(InstrumentType.SingleStock)
			.universe(universe)
			.identifiers(identifiers)
			.requestType(RequestType.HistoricalDataRequest)
			.startDate(startDate)
			.endDate(endDate)
			.windowLookForward(requestLookForward)
			.step(requestStep)
			.priority(-1)
			.build();	
		}
					
	}

	@Test
	public void testInstrumentStateSequencer() throws TimeoutException  {	
		//Logger.getRootLogger().setLevel(Level.OFF);
		
		// 3 - Capture state
		sequencerCapture = (InstrumentStateCaptureSequencer) new InstrumentStateCaptureSequencer.Builder()
				.spotDataFields(ArrayUtils.addAll(fields,fieldWithOverrides))
				.instrumentType(InstrumentType.SingleStock)
				.identifierTypes(IdentifierType.valueOf("TICKER"))
				.universe(universe)
				.identifiers(identifiers)								
				.startDate(startDate)
				.endDate(endDate)
				.step(Duration.ofDays(1))		
				.priority(0)
				.build();

		Function<Dataset<Row>,Dataset<Row>> watermarking =  (df) -> df.withWatermark("datetime", "3 days");
		List<Function<Dataset<Row>,Dataset<Row>>> transforms = new ArrayList<Function<Dataset<Row>,Dataset<Row>>>();
		transforms.add(watermarking);
		
		// 4  - stream writer
		sequencerQuery = (StreamQuerySequencer) new StreamQuerySequencer.Builder()
				.memoryStream(sequencerCapture.getMemoryStream())
				.format("parquet")
				.path(exportPath)
				.transforms(transforms)
				.checkpointLocation(checkpointPath)	
				.partitionColumns(partitionColumns)				
				.outputMode("append")				
				.queryName("myStreamQuery")
				.startDate(startDate)
				.endDate(endDate)
				.step(Duration.ofDays(1))
				.priority(1)
				.build();
		
		sequencerQuery.getQuery().setPrint(false);

		CoreConfig.services().run();
	}


	
}
