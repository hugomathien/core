package timers;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.TimerDataframeWriterBatch;
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

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestBatchWriter {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private DataRequest request;
	private TimerDataframeWriterBatch writerBatch;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		request = new DataRequest.Builder()
				.dataService(DataServiceEnum.RANDOMGEN)
				.backfill(false)
				.fields("VOLUME","PX_LAST")
				.parameters(RequestParameters.UseDPDF, false)
				.parameters(RequestParameters.startDate, LocalDate.of(2022,1,1))
				.parameters(RequestParameters.endDate, LocalDate.of(2022, 1, 31))
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.SingleStock, new String[]{"TTE FP","VOD LN"})
				.requestType(RequestType.HistoricalDataRequest)
				.build();

		request.query();
		//CoreConfig.services().run();
	}

	@Test
	public void testInstrumentStateCaptureSequencer() throws TimeoutException, InterruptedException {

		dataset.StateToDataframe capture = new dataset.StateToDataframe.Builder()
				.dfContainerType(dataset.StateToDataframe.DFContainerType.BATCH)
				.instrumentType(InstrumentType.SingleStock)
				.identifierTypes(IdentifierType.TICKER)
				.spotDataFields("VOLUME","PX_LAST")
				.identifiers("TTE FP","VOD LN")
				.build();

		sequencer = new TimerStateToDataframe.Builder()
		.stateCapture(capture)
		.startDate(LocalDate.of(2022, 1, 1))
		.endDate(LocalDate.of(2022, 1, 31))
		.step(Duration.ofDays(1))
		.build();

		writerBatch = (TimerDataframeWriterBatch) new TimerDataframeWriterBatch.Builder()
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\hugom\\Downloads\\test.csv")
				.format("console")
				.outputMode("append")
				.queryName("myQuery")
				.startDate(LocalDate.of(2022, 1, 1))
				.endDate(LocalDate.of(2022, 1, 31))
				.step(Duration.ofDays(1))
				.build();

		CoreConfig.services().run();
		Thread.sleep(20000);
	}
	
	
}
