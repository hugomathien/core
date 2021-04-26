package bloomberg;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Before;
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
import marketdata.services.bloomberg.BBGReferenceDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestBBGHistoricalDataRequest {

	private DataRequest<?> request;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		request = new DataRequest.Builder<>()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(false)
				.fields("VOLUME","FIELD_EXCEPTION_ERROR","PX_LAST")
				.parameters(RequestParameters.UseDPDF, false)
				.parameters(RequestParameters.startDate, LocalDate.of(2021,4,5))
				.parameters(RequestParameters.endDate, LocalDate.of(2021, 4, 9))
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.SingleStock, new String[]{"FP FP","VOD LN"})
				.requestType(RequestType.HistoricalDataRequest)
				.build();

		request.query();
		CoreConfig.services().run();
	}

	@Test
	public void testFieldMapSize() throws DataServiceStartException, DataQueryException {	
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

		Assert.assertEquals(totalFieldMapSize,2);
		Assert.assertEquals(vodafoneFieldMap,2);
	}

	@Test
	public void testSpotPriceValue() throws DataServiceStartException, DataQueryException {	
		double totalSpotPrice = (double) CoreConfig.services().instrumentFactory()
				.getInstrument("FP FP")
				.getMarketData()
				.getSpot()
				.get("PX_LAST");

		double vodafoneSpotPrice = (double) CoreConfig.services().instrumentFactory()
				.getInstrument("VOD LN")
				.getMarketData()
				.getSpot()
				.get("PX_LAST");
		
		
		Assert.assertEquals(totalSpotPrice, 37.845, 0.0);
		Assert.assertEquals(vodafoneSpotPrice, 134.64, 0.0);

	}

}
