package timers.dfwriters;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.*;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestFX {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private DataRequest dataRequest;
	private TimerDataframeWriterStream writerStream;
	private String[] fx = {"EURUSD","GBPUSD","NOKUSD","SEKUSD","CHFUSD","DKKUSD"};
	@Before
	public void setup() throws DataQueryException, DataServiceStartException {

		dataRequest = new DataRequest.Builder()
				.fields("PX_LAST")
				.instrumentType(InstrumentType.FX)
				.identifierType(IdentifierType.TICKER)
				.identifiers(fx)
				.dataService(DataServiceEnum.BLOOMBERG)
				.requestType(RequestType.HistoricalDataRequest)
				.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
				.parameters(RequestParameters.endDate,  CoreConfig.GLOBAL_END_DATE)
				.build();

		dataRequest.query();
	}

	@Test
	public void testInstrumentStateCaptureSequencer() throws TimeoutException, InterruptedException {

		StateToDataframe capture = new dataset.StateToDataframe.Builder()
				.dfContainerType(dataset.StateToDataframe.DFContainerType.MEMORY_STREAM)
				.instrumentType(InstrumentType.FX)
				.identifierTypes(IdentifierType.TICKER)
				.spotDataFields("PX_LAST")
				.identifiers(fx)
				.build();

		sequencer = new TimerStateToDataframe.Builder()
				.stateCapture(capture)
				.step(Duration.ofDays(1))
				.build();

		writerStream = (TimerDataframeWriterStream) new TimerDataframeWriterStream.Builder()
				.async(true)
				.processingTime("10 seconds")
				.checkpointLocation("C:\\Users\\admin\\Documents\\workspace\\datalake\\checkpoint")
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\admin\\Documents\\workspace\\datalake\\parquet\\fx")
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
