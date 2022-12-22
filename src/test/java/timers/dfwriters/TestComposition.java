package timers.dfwriters;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.*;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.*;
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
	private TimerDataframeWriterBatch writerBatch;
	private String index = "SXXP";
	private String region = "emea";
	private String format="parquet";
	private String exportFileName = "composition";
	private boolean replaceWithComposite = false;
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
		.dfContainerType(StateToDataframe.DFContainerType.BATCH)
		.instrumentType(InstrumentType.SingleStock)
		.identifierTypes(IdentifierType.TICKER)
		.universe(index)
		.expandingUniverse(false)
		.replaceIdentifiersWithComposite(replaceWithComposite)
		.build();

		new TimerStateToDataframe.Builder()
		.stateCapture(capture)
		.step(Duration.ofDays(1))
		.build();

		writerBatch = (TimerDataframeWriterBatch) new TimerDataframeWriterBatch.Builder()
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\admin\\Documents\\workspace\\data\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\"+exportFileName)
				.format(format)
				.outputMode("append")
				.partitionColumns(new String[]{"year"})
				.startDate(CoreConfig.GLOBAL_START_DATE)
				.endDate(CoreConfig.GLOBAL_END_DATE)
				.step(Duration.ofDays(365))
				.runOnceOnTermination(true)
				.build();

		CoreConfig.services().run();

	}

	
}
