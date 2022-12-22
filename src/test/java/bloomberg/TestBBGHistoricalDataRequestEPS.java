package bloomberg;

import java.time.LocalDate;

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
import marketdata.timeseries.DayData;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.utils.RequestOverrides;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBBGHistoricalDataRequestEPS {

	private DataRequest request;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		request = new DataRequest.Builder()
				.dataService(DataServiceEnum.BLOOMBERG)
				.backfill(true)
				.fields("BEST_EPS_MEDIAN_1BF")
				.override(RequestOverrides.BEST_FPERIOD_OVERRIDE, "1BF")
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
		DayData ts = CoreConfig.services().instrumentFactory().getInstrument("DBK GY")
		.getMarketData()
		.getHistorical()
		.getEodData();
		
		ts.printTimeSeries();
		
		double eps1bf = (double) ts.getEod("2017-03-21").get("BEST_EPS_MEDIAN_1BF");
		Assert.assertEquals(eps1bf, 1.241,0);
	}
	
	



}
