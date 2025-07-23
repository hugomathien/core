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
		.fields("LATEST_ANNOUNCEMENT_DT","ANNOUNCEMENT_DT","LATEST_PERIOD_END_DT_FULL_RECORD","TRAILING_12M_SALES_GROWTH")
		.instrumentType(InstrumentType.SingleStock)
		.identifierType(IdentifierType.TICKER)
		.identifiers("ASML NA")
		.backfill(true)
		.dataService(DataServiceEnum.BLOOMBERG)
		.requestType(RequestType.HistoricalDataRequest)
		.parameters(RequestParameters.UseDPDF, true)
		.parameters(RequestParameters.startDate, LocalDate.of(2010,1,1))
		.parameters(RequestParameters.endDate, LocalDate.now())
		.override(RequestOverrides.FUND_PER, "Q")
		.override(RequestOverrides.FILING_STATUS, "OR")
		.override(RequestOverrides.EQY_FUND_CRNCY, "USD")
		.build();

		request.query();
		CoreConfig.services().run();
		
	
	}

	@Test
	public void testDataSeriesSize() {
		IInstrument stock = CoreConfig.services().getInstrument("ASML NA");
		TimeSeries<Instant,Day> ts = stock.getMarketData().getHistorical().getEodData().getTimeSeries();
		System.out.println("ASML NA Time Series");
		ts.printTimeSeries();
		Assert.assertEquals(ts.size(), 67);
	}

	
}
