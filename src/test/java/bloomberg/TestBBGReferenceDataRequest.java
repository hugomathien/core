package bloomberg;

import java.time.LocalDate;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.annotation.Order;
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
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.enumeration.RequestOverrides;
import org.junit.runners.MethodSorters;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBBGReferenceDataRequest {

	private DataRequest<Object> request;


	@Test
	public void a() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields("PX_LAST","VOLUME")
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.SingleStock, new String[]{"FP FP","VOD LN"})
		.requestType(RequestType.ReferenceDataRequest)
		.build();
		
		request.query();
		CoreConfig.services().run();
	}
	
	@Test
	public void b() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields("INDX_MWEIGHT_HIST")
		.override(RequestOverrides.END_DATE_OVERRIDE, LocalDate.of(2021, 3, 1))
		.identifierType(IdentifierType.TICKER)
		.identifiers(InstrumentType.Index, new String[]{"SXXP"})
		.requestType(RequestType.ReferenceDataRequest)
		.build();
		
		request.query();
		CoreConfig.services().run();
	}
	
	@Test
	public void c() throws DataServiceStartException, DataQueryException {
		request = new DataRequest.Builder<>()
		.dataService(DataServiceEnum.BLOOMBERG)
		.backfill(false)
		.fields("PX_LAST")
		.identifierType(IdentifierType.TICKER)
		.universe("SXXP")
		.requestType(RequestType.ReferenceDataRequest)
		.build();
		
		request.query();
		CoreConfig.services().run();
		
		CoreConfig.services().instrumentFactory().
		getInstrumentSet().stream().filter(i -> i.getInstrumentType().equals(InstrumentType.SingleStock))
		.forEach(i -> System.out.println(i.toString() +"="+i.getSpot(Field.get("PX_LAST")).toString()));
	}

	
	
}
