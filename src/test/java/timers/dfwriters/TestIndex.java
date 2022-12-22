package timers.dfwriters;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.AbstractEventSequencer;
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
public class TestIndex {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private DataRequest requestTechnical;
	private TimerDataframeWriterStream writerStream;
	private String[] fields = {"PX_LAST","OPEN","HIGH","LOW","VWAP"};
	private String[] identifiers = {
			"SPTR",
			"RU20INTR",
			"SXXR",
			"SX5T",
			"TUKXG",
			"CACR",
			"FTSEMIBN",
			"AEX",
			"DAX",
			"PSI20TR",
			"IBEX35TR",
			"ATXTR",
			"BEL20G",
			"ASESAGD",
			"KAX",
			"OMXS30GI",
			"OBX",
			"HEXY"};
	@Before
	public void setup() throws DataQueryException, DataServiceStartException {

		requestTechnical = new DataRequest.Builder()
				.dataService(DataServiceEnum.BLOOMBERG)
				.requestType(RequestType.HistoricalDataRequest)
				.fields(fields)
				.parameters(RequestParameters.UseDPDF, true)
				.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
				.parameters(RequestParameters.endDate,  CoreConfig.GLOBAL_END_DATE)
				.identifierType(IdentifierType.TICKER)
				.instrumentType(InstrumentType.Index)
				.identifiers(identifiers)
				.build();

		requestTechnical.query();

	}

	@Test
	public void testWriter() throws TimeoutException, InterruptedException {

		StateToDataframe capture = new StateToDataframe.Builder()
				.dfContainerType(StateToDataframe.DFContainerType.MEMORY_STREAM)
				.instrumentType(InstrumentType.Index)
				.identifierTypes(IdentifierType.TICKER)
				.spotDataFields(fields)
				.identifiers(identifiers)
				.build();

		sequencer = new TimerStateToDataframe.Builder()
		.stateCapture(capture)
		.step(Duration.ofDays(1))
		.build();

		writerStream = (TimerDataframeWriterStream) new TimerDataframeWriterStream.Builder()
				.async(true)
				.processingTime("10 seconds")
				.checkpointLocation("C:\\Users\\admin\\Documents\\workspace\\data\\checkpoint")
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\admin\\Documents\\workspace\\data\\parquet\\index\\geo\\technical")
				.queryName("query")
				.format("parquet")
				.outputMode("append")
				.partitionColumns(new String[]{"year"})
				.step(Duration.ofDays(365))
				.build();

		CoreConfig.services().run();
		writerStream.getQuery().processAllAvailable();
		writerStream.getQuery().stop();
	}
	
	
}
