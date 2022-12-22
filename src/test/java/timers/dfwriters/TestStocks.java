package timers.dfwriters;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.AbstractEventSequencer;
import event.timers.TimerStateToDataframe;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import event.timers.TimerDataframeWriterStream;

import java.time.Duration;
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
	private String[] fields = {"PX_LAST","OPEN","HIGH","LOW","VWAP","VOLUME","OFFICIAL_OPEN_AUCTION_VOLUME","OFFICIAL_CLOSE_AUCTION_VOLUME"};
	private String exportFileName = "technical";
	private String region="amrs";
	private String index="RAY";
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

		dataset.StateToDataframe capture = new dataset.StateToDataframe.Builder()
				.dfContainerType(dataset.StateToDataframe.DFContainerType.MEMORY_STREAM)
				.instrumentType(InstrumentType.SingleStock)
				.identifierTypes(IdentifierType.TICKER)
				.spotDataFields(fields)
				.identifiers(this.requestTechnical.getIdentifiers().stream().map(i -> i.getName()).toArray(String[]::new))
				.build();

		sequencer = new TimerStateToDataframe.Builder()
		.stateCapture(capture)
		.step(Duration.ofDays(1))
		.build();

		writerStream = (TimerDataframeWriterStream) new TimerDataframeWriterStream.Builder()
				.async(true)
				.processingTime("120 seconds")
				.checkpointLocation("C:\\Users\\admin\\Documents\\workspace\\datalake\\checkpoint")
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\admin\\Documents\\workspace\\datalake\\"+format+"\\equity\\"+region+"\\"+index+"\\"+exportFileName)
				.queryName("query")
				.format(format)
				.outputMode("append")
				.partitionColumns(new String[]{"year"})
				.step(Duration.ofDays(365))
				.build();

		CoreConfig.services().run();
		writerStream.getQuery().processAllAvailable();
		writerStream.getQuery().stop();
	}
	
	
}
