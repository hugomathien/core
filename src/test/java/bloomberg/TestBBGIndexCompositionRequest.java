package bloomberg;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.instruments.IPortfolio;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.enumeration.RequestOverrides;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestBBGIndexCompositionRequest {

	private DataRequest<Object> request;


	
	@Before
	public void setup() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields("INDX_MWEIGHT_HIST")
		.override(RequestOverrides.END_DATE_OVERRIDE, LocalDate.of(2002, 1, 2))
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.Index, new String[]{"SXXP"})
		.requestType(RequestType.ReferenceDataRequest)
		.build();
		
		request.query();
		CoreConfig.services().run();
	}
	
	@Test
	public void testUniverseSize() {
		long stockUniverseSize = CoreConfig.services()
		.instrumentFactory()
		.getInstrumentSet()
		.stream()
		.filter(i -> i.getInstrumentType().equals(InstrumentType.SingleStock)).count();
		
		Assert.assertEquals(stockUniverseSize, 600);
	}
	
	@Test
	public void testIndexSize() {
		IPortfolio sxxp = CoreConfig.services()
		.getOrMakeIndex("SXXP");
		
		Assert.assertEquals(sxxp.getComposition().size(), 600);
	}
	
	@Test
	public void testIndexWeightSize() {
		IPortfolio sxxp = CoreConfig.services()
		.getOrMakeIndex("SXXP");
		
		Assert.assertEquals(sxxp.getWeights().keySet().size(),600);
	}

}
