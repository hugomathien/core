package sequencer;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

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
import event.sequencing.AbstractEventSequencer;
import event.sequencing.DataRequestSequencer;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentType;
import marketdata.container.Day;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestType;
import org.springframework.test.context.junit4.SpringRunner;
import utils.TimeSeries;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestDataRequestSequencer {

	private AbstractEventSequencer<DataRequest<Object>> sequencer;


	@Before
	public void testDataRequestSequencer() throws TimeoutException  {
		sequencer = new DataRequestSequencer.Builder<Object>()
		.fields("PX_LAST","VOLUME")
		.instrumentType(InstrumentType.SingleStock)
		.identifierType(IdentifierType.TICKER)
		.identifiers("TTE FP","VOD LN")
		.backfill(true)
		.dataService(DataServiceEnum.RANDOMGEN)
		.requestType(RequestType.HistoricalDataRequest)
		.startDate(LocalDate.of(2021, 3, 15))
		.endDate(LocalDate.of(2021, 3, 31))
		.step(Duration.ofDays(1))
		.initialWindowLookBack(Duration.ofDays(30))			
		.build();
		
		CoreConfig.services().run();
		
	
	}

	@Test
	public void testTotalDataSeriesSize() {
		IInstrument total = CoreConfig.services().getInstrument("TTE FP");
		TimeSeries<Instant,Day> ts = total.getMarketData().getHistorical().getEodData().getTimeSeries();
		System.out.println("TTE FP Time Series");
		ts.printTimeSeries();
		Assert.assertEquals(33,ts.size());
	}
	
	@Test
	public void testVodafoneDataSeriesSize() {
		IInstrument total = CoreConfig.services().getInstrument("VOD LN");
		TimeSeries<Instant,Day> ts = total.getMarketData().getHistorical().getEodData().getTimeSeries();
		System.out.println("VOD LN Time Series");
		ts.printTimeSeries();
		Assert.assertEquals(33,ts.size());
	}
	
	
}
