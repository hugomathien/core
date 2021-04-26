package randomgen;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import exceptions.DataServiceStartException;
import marketdata.services.bloomberg.BBGReferenceDataService;
import marketdata.services.randomgen.RandomGeneratorReferenceDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestRandomGenService {


    @Autowired
    private RandomGeneratorReferenceDataService randomGenService;
 
 
    @Test
    public void testSampleService() throws DataServiceStartException {
    	randomGenService.start();
       assertTrue(randomGenService.isOpened());
    }
 
  
}
