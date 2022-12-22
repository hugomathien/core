package timers.dfwriters;

import config.CoreConfig;
import dataset.StateToDataframe;
import event.timers.AbstractEventSequencer;
import event.timers.TimerDataframeWriterBatch;
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
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestStaticData {

	private AbstractEventSequencer<StateToDataframe> sequencer;
	private DataRequest requestComposition;
	private DataRequest requestTechnical;
	private TimerDataframeWriterBatch writerBatch;
	private String[] fields = {"CRNCY","EXCH_CODE","INDUSTRY_SECTOR","INDUSTRY_GROUP"};
	//private String[] fields = {"PX_LAST","VOLUME"};
	private String region = "emea";
	private String index = "SXXP";
	private String format = "parquet";
	private boolean replaceWithComposite = false;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		requestComposition = new DataRequest.Builder()
				.dataService(DataServiceEnum.FLAT_FILE)
				.parameters("filepath","C:\\Users\\admin\\Documents\\workspace\\data\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\composition")
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

		requestTechnical = new DataRequest.Builder()
				.dataService(DataServiceEnum.BLOOMBERG)
				.requestType(RequestType.ReferenceDataRequest)
				.fields(fields)
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
				.watermark(Duration.ofDays(10))
				.build();

		sequencer = new TimerStateToDataframe.Builder()
		.stateCapture(capture)
		.startDate(LocalDate.now().plusDays(1))
		.endDate(LocalDate.now().plusDays(1))
		.step(Duration.ofDays(1))
		.build();

		writerBatch = (TimerDataframeWriterBatch) new TimerDataframeWriterBatch.Builder()
				.dfContainer(capture.getDfContainer())
				.path("C:\\Users\\admin\\Documents\\workspace\\data\\"+format+"\\equity\\"+region+"\\"+index.toLowerCase()+"\\static_data")
				.format(this.format)
				.outputMode("append")
				.partitionColumns(new String[]{"year"})
				.startDate(LocalDate.now().plusDays(1))
				.endDate(LocalDate.now().plusDays(1))
				.step(Duration.ofDays(1))
				.build();

		CoreConfig.services().run();
	}

}
