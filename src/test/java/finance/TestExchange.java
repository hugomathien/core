package finance;

import config.CoreConfig;
import exceptions.DataServiceStartException;
import finance.identifiers.IdentifierType;
import finance.identifiers.Ticker;
import finance.instruments.IInstrument;
import finance.instruments.Instrument;
import finance.instruments.InstrumentType;
import finance.misc.Exchange;
import marketdata.services.randomgen.RandomGeneratorReferenceDataService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestExchange {
    @Autowired
    private CoreConfig config;
    @Autowired(required = false)
    public Map<String,Exchange> exchangeCodeMap;
 
    @Test
    public void testExchange() throws DataServiceStartException {
    	Exchange UW = config.getExchange("UW");
        Exchange FP = config.getExchange("FP");
    }

    @Test
    public void replaceWithComposite() throws DataServiceStartException {
        Instrument stock = config.getOrMakeSingleStock("AAPL VF");
        String newId = stock.replaceIdentifierWithComposite(IdentifierType.TICKER,"AAPL UW");

    }
}
