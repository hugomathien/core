package sequencer;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class TestDataRequestSequencerXML {

	@Autowired
	@Qualifier("datarequest1")
	private DataRequestSequencer<Object> sequencer;

	@Test
	public void testDataRequestSequencer() throws TimeoutException  {

		CoreConfig.services().run();
	
	}
	
}
