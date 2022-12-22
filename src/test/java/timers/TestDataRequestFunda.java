package timers;

import config.CoreConfig;
import event.timers.AbstractEventSequencer;
import event.timers.TimerDataRequest;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentType;
import marketdata.container.Day;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.utils.RequestOverrides;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import utils.TimeSeries;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestDataRequestFunda {

	private DataRequest request;


	@Before
	public void testDataRequestSequencer() throws TimeoutException, DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder()
		.fields("ANNOUNCEMENT_DT","LATEST_PERIOD_END_DT_FULL_RECORD")
		.instrumentType(InstrumentType.SingleStock)
		.identifierType(IdentifierType.TICKER)
		.identifiers("TNTRQ US")
		.backfill(true)
		.dataService(DataServiceEnum.BLOOMBERG)
		.requestType(RequestType.HistoricalDataRequest)
		.parameters(RequestParameters.UseDPDF, true)
		.parameters(RequestParameters.startDate, CoreConfig.GLOBAL_START_DATE)
		.parameters(RequestParameters.endDate, CoreConfig.GLOBAL_END_DATE)
		.override(RequestOverrides.FUND_PER, "Q")
		.override(RequestOverrides.FILING_STATUS, "OR")
		.build();

		request.query();
		CoreConfig.services().run();
		
	
	}

	@Test
	public void testDataSeriesSize() {
		IInstrument stock = CoreConfig.services().getInstrument("A US");
		TimeSeries<Instant,Day> ts = stock.getMarketData().getHistorical().getEodData().getTimeSeries();
		System.out.println("AAPL US Time Series");
		ts.printTimeSeries();
		Assert.assertEquals(33,ts.size());
	}

	
}
