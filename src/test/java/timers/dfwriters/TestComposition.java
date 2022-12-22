package timers.dfwriters;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.TimerDataframeWriterStream;
import event.timers.AbstractEventSequencer;
import event.timers.TimerDataRequest;
import event.timers.TimerStateToDataframe;
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
public class TestComposition {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private AbstractEventSequencer<DataRequest> request;
	private TimerDataframeWriterStream writerStream;
	private String index = "RAY";
	private String region = "amrs";
	private String format="parquet";
	@Before
	public void testDataRequestSequencer() throws TimeoutException  {
		request = new TimerDataRequest.Builder()
				.identifierType(IdentifierType.TICKER)
				.instrumentType(InstrumentType.Index)
				.identifiers(index)
				.dataService(DataServiceEnum.BLOOMBERG)
				.requestType(RequestType.UniverseRequest)
				.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
				.parameters(RequestParameters.endDate, CoreConfig.GLOBAL_END_DATE)
				.step(Duration.ofDays(91))
				.build();

	}

	@Test
	public void testInstrumentStateCaptureSequencer() throws TimeoutException, InterruptedException {

		StateToDataframe capture = new dataset.StateToDataframe.Builder()
		.dfContainerType(dataset.StateToDataframe.DFContainerType.MEMORY_STREAM)
		.instrumentType(InstrumentType.SingleStock)
		.identifierTypes(IdentifierType.TICKER)
		.universe(index)
		.expandingUniverse(false)
		.build();

		new TimerStateToDataframe.Builder()
		.stateCapture(capture)
		.step(Duration.ofDays(1))
		.build();

		writerStream = (TimerDataframeWriterStream) new TimerDataframeWriterStream.Builder()
		.processingTime("60 seconds")
		.checkpointLocation("C:\\Users\\admin\\Documents\\workspace\\datalake\\checkpoint")
		.dfContainer(capture.getDfContainer())
		.path("C:\\Users\\admin\\Documents\\workspace\\datalake\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\composition")
		.format("parquet")
		.outputMode("append")
		.queryName("myQuery")
		.partitionColumns(new String[]{"year"})
		.step(Duration.ofDays(365))
		.build();

		CoreConfig.services().run();
		writerStream.getQuery().processAllAvailable();
		writerStream.getQuery().stop();
	}

	
}
