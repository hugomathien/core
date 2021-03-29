package bloomberg;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
import marketdata.field.Field;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.services.BBGReferenceDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestBBGIntradayBarDataRequest {

	private DataRequest request;

	@Test
	public void testSampleService() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields(Field.get("TRADE"))
		.parameters(RequestParameters.startDateTime, ZonedDateTime.of(LocalDateTime.of(2021, 3, 25, 10, 30, 0), ZoneId.of("Europe/London")))
		.parameters(RequestParameters.endDateTime, ZonedDateTime.of(LocalDateTime.of(2021, 3, 25, 11, 30, 0), ZoneId.of("Europe/London")))
		.parameters(RequestParameters.interval, 1)
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.SingleStock, new String[]{"FPSD FP","VOD LN"})
		.requestType(RequestType.IntradayBarRequest)
		.build();
		
		request.query();
		
		System.out.println("completed");
	}

}
