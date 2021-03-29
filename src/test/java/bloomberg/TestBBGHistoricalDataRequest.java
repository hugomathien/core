package bloomberg;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.services.BBGReferenceDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestBBGHistoricalDataRequest {

	private DataRequest request;


	@Test
	public void testSampleService() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields("PX_LAST","VOLUME")
		.parameters(RequestParameters.startDate, LocalDate.now().minusDays(300))
		.parameters(RequestParameters.endDate, LocalDate.now().minusDays(250))
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.SingleStock, new String[]{"FP FP","AI FP"})
		.requestType(RequestType.HistoricalDataRequest)
		.build();
		
		request.query();
	}

}
