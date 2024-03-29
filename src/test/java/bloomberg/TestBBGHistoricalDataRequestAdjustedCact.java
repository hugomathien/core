package bloomberg;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
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
import marketdata.services.bloomberg.BBGReferenceDataService;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBBGHistoricalDataRequestAdjustedCact {

	private DataRequest request;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		request = new DataRequest.Builder()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(true)
				.fields("PX_LAST","VOLUME")
				.parameters(RequestParameters.adjustmentAbnormal, true)
				.parameters(RequestParameters.adjustmentNormal, true)
				.parameters(RequestParameters.adjustmentSplit, true)
				.parameters(RequestParameters.startDate, LocalDate.of(2017,3,18))
				.parameters(RequestParameters.endDate, LocalDate.of(2017, 3, 25))
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.SingleStock, new String[]{"DBK GY"})
				.requestType(RequestType.HistoricalDataRequest)
				.build();

		request.query();
		CoreConfig.services().run();
	}

	@Test
	public void testAdjustedPrice() throws DataServiceStartException, DataQueryException {	
		double price = (double) CoreConfig.services().instrumentFactory().getInstrument("DBK GY")
		.getMarketData()
		.getHistorical()
		.getEodData()
		.getEod("2017-03-21").get("PX_LAST");
		
		Assert.assertEquals(price, 15.394,0);
	}
	
	



}
