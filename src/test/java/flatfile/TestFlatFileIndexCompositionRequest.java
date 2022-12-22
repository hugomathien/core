package flatfile;

import config.CoreConfig;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestFlatFileIndexCompositionRequest {

	private DataRequest request;


	
	@Before
	public void setup() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder()
		.dataService(DataServiceEnum.FLAT_FILE)
		.backfill(false)
		.parameters(RequestParameters.startDate, LocalDate.of(2002, 1, 2))
		.parameters(RequestParameters.endDate, LocalDate.of(2004, 1, 2))
		.parameters("filepath","C:\\Users\\admin\\Documents\\workspace\\datalake\\emea\\composition\\sxxp_stream_2")
		.parameters("fileformat","csv")
		.instrumentType(InstrumentType.Index)
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.Index, new String[]{"SXXP"})
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
		
		Assert.assertEquals(725,stockUniverseSize);
	}


}
