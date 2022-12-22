package timers;

import config.CoreConfig;
import event.timers.TimerDataRequest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreConfig.class)
@FixMethodOrder(MethodSorters.DEFAULT)
public class TestDataRequestXML {

	@Autowired
	@Qualifier("datarequest1")
	private TimerDataRequest sequencer;

	@Test
	public void testDataRequestSequencer() throws TimeoutException  {

		CoreConfig.services().run();
	
	}
	
}
