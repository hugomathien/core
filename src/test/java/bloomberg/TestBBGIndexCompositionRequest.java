package bloomberg;

import java.time.LocalDate;

import marketdata.services.base.RequestParameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;

import config.CoreConfig;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestType;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBBGIndexCompositionRequest {

	private DataRequest request;


	
	@Before
	public void setup() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder()
		.dataService(DataServiceEnum.BLOOMBERG)
		.parameters(RequestParameters.startDate, LocalDate.of(2002, 1, 2))
		.parameters(RequestParameters.endDate, LocalDate.of(2002, 1, 2))
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.Index, new String[]{"SX5E"})
		.requestType(RequestType.UniverseRequest)
		.build();
		
		request.query();
		CoreConfig.services().run();
	}
	
	@Test
	public void testUniverseSize() {
		long stockUniverseSize = CoreConfig.services()
		.instrumentFactory()
		.getInstrumentSet()
		.stream()
		.filter(i -> i.getInstrumentType().equals(InstrumentType.SingleStock)).count();
		
		Assert.assertEquals(50,stockUniverseSize);
	}

}
