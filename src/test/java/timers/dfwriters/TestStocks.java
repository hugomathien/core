package timers.dfwriters;

import com.google.common.collect.Lists;
import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.AbstractEventSequencer;
import event.timers.TimerDataframeWriterBatch;
import event.timers.TimerStateToDataframe;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.Identifier;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import event.timers.TimerDataframeWriterStream;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestStocks {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private DataRequest requestComposition;
	private DataRequest requestTechnical;
	private TimerDataframeWriterStream writerStream;
	private TimerDataframeWriterBatch writerBatch;
	Map<String, String[]> fieldsMap = new HashMap<String, String[]>() {{
/*
		put("technical", new String[]{"PX_LAST","OPEN","HIGH","LOW","VWAP","VOLUME","OFFICIAL_OPEN_AUCTION_VOLUME","OFFICIAL_CLOSE_AUCTION_VOLUME","EQY_SH_OUT","EQY_FREE_FLOAT_PCT"});
*/
		put("analyst_recommendations", new String[]{
				"TOT_ANALYST_REC","TOT_BUY_REC","TOT_SELL_REC","TOT_HOLD_REC","EQY_REC_CONS"});
	}};
	private String exportFileName = "technical";
	private String region="emea";
	private String index="SXXP";
	private String format="parquet";
	private int batchSize = 3000;
	private boolean replaceWithComposite = false;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		requestComposition = new DataRequest.Builder()
				.dataService(DataServiceEnum.FLAT_FILE)
				.parameters("filepath","C:\\Users\\hugom\\workspace\\datalake\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\composition")
				.parameters("fileformat",format)
				.instrumentType(InstrumentType.Index)
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.Index, new String[]{index})
				.requestType(RequestType.UniverseRequest)
				.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
				.parameters(RequestParameters.endDate,  CoreConfig.GLOBAL_END_DATE)
				.parameters(RequestParameters.useComposite,replaceWithComposite)
				.build();

		requestComposition.query();

	}

	@Test
	public void testWriter() throws TimeoutException, InterruptedException, DataServiceStartException, DataQueryException, IOException {

		for(Map.Entry<String, String[]> entry : fieldsMap.entrySet()) {
			String exportFileName = entry.getKey();
			String[] fields = entry.getValue();
			List<Identifier> idList = CoreConfig.services().instrumentFactory()
					.getInstrumentSet()
					.stream()
					.filter(p -> p.getInstrumentType().equals(InstrumentType.SingleStock))
					.map(i -> i.getIdentifier(IdentifierType.TICKER))
					.collect(Collectors.toList());

			List<List<Identifier>> batches = Lists.partition(idList, batchSize);

			for (List<Identifier> idBatch : batches) {
				requestTechnical = new DataRequest.Builder()
						.dataService(DataServiceEnum.BLOOMBERG)
						.requestType(RequestType.HistoricalDataRequest)
						.fields(fields)
						.parameters(RequestParameters.UseDPDF, true)
						.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
						.parameters(RequestParameters.endDate, CoreConfig.GLOBAL_END_DATE)
						.identifierType(IdentifierType.TICKER)
						.instrumentType(InstrumentType.SingleStock)
						.identifiers(idBatch)
						.build();

				requestTechnical.query();

				dataset.StateToDataframe capture = new dataset.StateToDataframe.Builder()
						.dfContainerType(StateToDataframe.DFContainerType.BATCH)
						.instrumentType(InstrumentType.SingleStock)
						.identifierTypes(IdentifierType.TICKER)
						.spotDataFields(fields)
						.identifiers(idBatch.stream().map(i -> i.getName()).toArray(String[]::new))
						.build();

				sequencer = new TimerStateToDataframe.Builder()
						.stateCapture(capture)
						.step(Duration.ofDays(1))
						.build();


				writerBatch = (TimerDataframeWriterBatch) new TimerDataframeWriterBatch.Builder()
						.dfContainer(capture.getDfContainer())
						.path("C:\\Users\\admin\\Documents\\workspace\\data\\" + format + "\\equity\\" + region + "\\" + index.toLowerCase() + "\\" + exportFileName)
						.format(format)
						.outputMode("append")
						.partitionColumns(new String[]{"year"})
						.startDate(CoreConfig.GLOBAL_START_DATE)
						.endDate(CoreConfig.GLOBAL_END_DATE)
						.step(Duration.ofDays(365))
						.runOnceOnTermination(true)
						.build();


				CoreConfig.services().run();
				System.gc();
			}

		}

	}
	
	
}
