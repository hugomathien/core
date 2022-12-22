package marketdata.services.bloomberg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Session;

import config.CoreConfig;
import event.events.MarketDataEventFactory;
import event.processing.EventPriorityQueue;
import finance.instruments.InstrumentFactory;
import marketdata.services.base.AbstractResponseHandler;
import marketdata.services.bloomberg.BBGRealTimeDataService;

@Service
@Scope("singleton")
@Lazy(true)
public class BBGRealTimeResponseHandler extends AbstractResponseHandler<BBGRealTimeDataService> implements EventHandler{
	@Autowired
	private CoreConfig contex;
	@Autowired
	private EventPriorityQueue eventQueue;
	@Autowired
	InstrumentFactory factory;
	@Autowired
	private MarketDataEventFactory marketDataEventFactory;
	
	public BBGRealTimeResponseHandler() {
		
	}
	
	@Override
	public void setDataService(BBGRealTimeDataService dataService) {
		this.dataService = dataService;
	}

	@Override
	public void processEvent(Event arg0, Session arg1) {
		// TODO Auto-generated method stub
		
	}
	

}
