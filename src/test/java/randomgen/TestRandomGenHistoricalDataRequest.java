package randomgen;

import java.time.LocalDate;

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
public class TestRandomGenHistoricalDataRequest {

	private DataRequest request;

	@Before
	public void setup() throws DataQueryException, DataServiceStartException {
		request = new DataRequest.Builder()
				.dataService(DataServiceEnum.RANDOMGEN)
				.backfill(false)
				.fields("PX_LAST","VOLUME")
				.backfill(true)
				.parameters(RequestParameters.startDate, LocalDate.of(2021,4,5))
				.parameters(RequestParameters.endDate, LocalDate.of(2021, 4, 9))
				.parameters(RequestParameters.randomizedNumberScales,new double[] {100.0,10000.0})
				.identifierType(IdentifierType.TICKER)
				.identifiers(InstrumentType.SingleStock, new String[]{"FP FP","VOD LN"})
				.requestType(RequestType.HistoricalDataRequest)
				.build();

		request.query();
		CoreConfig.services().run();
	}

	@Test
	public void testPrintRandomTimeSeries() throws DataServiceStartException, DataQueryException {	
		CoreConfig.services()
		.instrumentFactory()
		.getInstrumentSet()
		.forEach(i -> i.getMarketData().getHistorical().getEodData().printTimeSeries());
	}

}
