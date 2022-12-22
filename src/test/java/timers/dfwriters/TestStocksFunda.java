package timers.dfwriters;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.AbstractEventSequencer;
import event.timers.TimerDataframeWriterBatch;
import event.timers.TimerDataframeWriterStream;
import event.timers.TimerStateToDataframe;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.utils.RequestOverrides;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestStocksFunda {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private DataRequest requestComposition;
	private DataRequest requestTechnical;
	private TimerDataframeWriterStream writerStream;
	private TimerDataframeWriterBatch writerBatch;
	private String[] fields = {"ANNOUNCEMENT_DT","LATEST_ANNOUNCEMENT_DT","LATEST_PERIOD_END_DT_FULL_RECORD","FISCAL_YEAR_PERIOD"};
	//private String[] fields = {"NORMALIZED_ROE","FCF_YOY_GROWTH","FCF_SEQ_GROWTH","INVENTORY_SEQ_GROWTH","INVENTORY_YOY_GROWTH"};
	//private String[] fields = {"RETURN_COM_EQY","RETURN_ON_ASSET","RETURN_ON_CAP","RETURN_ON_INV_CAPITAL"};

	/*private String[] fields = {"RETURN_COM_EQY","RETURN_ON_ASSET","RETURN_ON_CAP","RETURN_ON_INV_CAPITAL",
			"GROSS_MARGIN","EBITDA_TO_REVENUE","OPER_MARGIN",
			"INCREMENTAL_OPERATING_MARGIN","PRETAX_INC_TO_NET_SALES",
			"INC_BEF_XO_ITEMS_TO_NET_SALES","PROF_MARGIN",
			"NET_INCOME_TO_COMMON_MARGIN","EFF_TAX_RATE",
			"DVD_PAYOUT_RATIO","SUSTAIN_GROWTH_RT"};*/


	private String exportFileName = "fundamental_margins";
	private String fund_per = "S";
	private String region = "emea";
	private String index = "SXXP";
	private String format="parquet";
	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		requestComposition = new DataRequest.Builder()
				.dataService(DataServiceEnum.FLAT_FILE)
				.parameters("filepath","C:\\Users\\admin\\Documents\\workspace\\datalake\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\composition")
				.parameters("fileformat",format)
				.instrumentType(InstrumentType.Index)
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.Index, new String[]{index})
				.requestType(RequestType.UniverseRequest)
				.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
				.parameters(RequestParameters.endDate,  CoreConfig.GLOBAL_END_DATE)
				.build();

		requestComposition.query();

		requestTechnical = new DataRequest.Builder()
				.dataService(DataServiceEnum.BLOOMBERG)
				.requestType(RequestType.HistoricalDataRequest)
				.fields(fields)
				.parameters(RequestParameters.UseDPDF, true)
				.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
				.parameters(RequestParameters.endDate,  CoreConfig.GLOBAL_END_DATE)
				.parameters(RequestParameters.nonTradingDayFillMethod,  "PREVIOUS_VALUE")
				.parameters(RequestParameters.nonTradingDayFillOption,  "ALL_CALENDAR_DAYS")
				.override(RequestOverrides.FUND_PER,fund_per)
				.override(RequestOverrides.FILING_STATUS,"OR")
				.identifierType(IdentifierType.TICKER)
				.instrumentType(InstrumentType.SingleStock)
				.identifiers(CoreConfig.services().instrumentFactory()
						.getInstrumentSet()
						.stream()
						.filter(p -> p.getInstrumentType().equals(InstrumentType.SingleStock))
						.map(i -> i.getIdentifier(IdentifierType.TICKER))
						.collect(Collectors.toSet()))
				.build();

		requestTechnical.query();

	}

	@Test
	public void testWriter() throws TimeoutException, InterruptedException {

		StateToDataframe capture = new StateToDataframe.Builder()
				.dfContainerType(StateToDataframe.DFContainerType.BATCH)
				.instrumentType(InstrumentType.SingleStock)
				.identifierTypes(IdentifierType.TICKER)
				.spotDataFields(fields)
				.identifiers(this.requestTechnical.getIdentifiers().stream().map(i -> i.getName()).toArray(String[]::new))
				.build();

		sequencer = new TimerStateToDataframe.Builder()
		.stateCapture(capture)
		.step(Duration.ofDays(1))
		.build();

/*		writerStream = (TimerDataframeWriterStream) new TimerDataframeWriterStream.Builder()
				.async(true)
				.processingTime("10 seconds")
				.checkpointLocation("C:\\Users\\admin\\Documents\\workspace\\datalake\\checkpoint")
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\admin\\Documents\\workspace\\datalake\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\"+exportFileName+"_"+fund_per)
				.queryName("query")
				.format(format)
				.outputMode("append")
				.partitionColumns(new String[]{"year"})
				.step(Duration.ofDays(365))
				.build();*/

		writerBatch = (TimerDataframeWriterBatch) new TimerDataframeWriterBatch.Builder()
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\admin\\Documents\\workspace\\datalake\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\"+exportFileName+"_"+fund_per)
				.format(format)
				.outputMode("append")
				.partitionColumns(new String[]{"year"})
				.startDate(CoreConfig.GLOBAL_START_DATE)
				.endDate(CoreConfig.GLOBAL_END_DATE)
				.step(Duration.ofDays(365))
				.build();

		CoreConfig.services().run();
		writerBatch.execute(CoreConfig.GLOBAL_END_DATE.atStartOfDay(CoreConfig.GLOBAL_ZONE_ID).toInstant());
		//writerStream.getQuery().processAllAvailable();
		//writerStream.getQuery().stop();
	}
	
	
}
