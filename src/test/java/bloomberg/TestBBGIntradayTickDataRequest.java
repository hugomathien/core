package bloomberg;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
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
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBBGIntradayTickDataRequest {

	private DataRequest request;

	@Before
	public void setup() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields(Field.get("BID"))
		.parameters(RequestParameters.startDateTime, ZonedDateTime.of(LocalDateTime.of(2021, 3, 25, 10, 30, 0), ZoneId.of("Europe/London")))
		.parameters(RequestParameters.endDateTime, ZonedDateTime.of(LocalDateTime.of(2021, 3, 25, 10, 31, 0), ZoneId.of("Europe/London")))
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.SingleStock, new String[]{"FP FP","VOD LN"})
		.requestType(RequestType.IntradayTickRequest)
		.build();
		
		request.query();
		CoreConfig.services().run();
	}
	
	
	@Test
	public void testSpotDataFromTick() {
		int totalFieldMapSize = CoreConfig.services().instrumentFactory()
				.getInstrument("FP FP")
				.getMarketData()
				.getSpot()
				.getFieldsMap()
				.size();

		int vodafoneFieldMap = CoreConfig.services().instrumentFactory()
				.getInstrument("VOD LN")
				.getMarketData()
				.getSpot()
				.getFieldsMap()
				.size();

		Assert.assertEquals(totalFieldMapSize,1);
		Assert.assertEquals(vodafoneFieldMap,1);
		
		
		System.out.println("FP FP " + CoreConfig.services().instrumentFactory()
				.getInstrument("FP FP")
				.getMarketData()
				.getSpot()
				.getFieldsMap()
				.toString());
		
		System.out.println("VOD LN" + CoreConfig.services().instrumentFactory()
				.getInstrument("VOD LN")
				.getMarketData()
				.getSpot()
				.getFieldsMap()
				.toString());
	}

}
