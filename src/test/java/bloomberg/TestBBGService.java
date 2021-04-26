package bloomberg;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import exceptions.DataServiceStartException;
import marketdata.services.bloomberg.BBGReferenceDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestBBGService {


    @Autowired
    private BBGReferenceDataService bbgService;
 
 
    @Test
    public void testSampleService() throws DataServiceStartException {
       bbgService.start();
       assertTrue(bbgService.isOpened());
    }
 
  
}
