package timers;

import config.CoreConfig;
import event.timers.AbstractEventSequencer;
import event.timers.TimerDataRequest;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
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
public class TestDataRequestIndexComposition {

	private AbstractEventSequencer<DataRequest> sequencer;

	@Test
	public void testDataRequestSequencer() throws TimeoutException  {
		sequencer = new TimerDataRequest.Builder()
		.fields("INDX_MWEIGHT_HIST")
		.identifierType(IdentifierType.TICKER)
		.instrumentType(InstrumentType.Index)
		.identifiers(new String[]{"SX5E"})
		.dataService(DataServiceEnum.BLOOMBERG)
		.requestType(RequestType.UniverseRequest)
		.parameters(RequestParameters.startDate, LocalDate.of(2022, 1, 15))
		.parameters(RequestParameters.endDate, LocalDate.of(2022, 1, 15))
		.startDate(LocalDate.of(2022, 1, 15))
		.endDate(LocalDate.of(2022, 1, 31))
		.step(Duration.ofDays(5))
		.build();

		CoreConfig.services().run();
	
	}
	
	
}
