package marketdata.services.base;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import events.MarketDataEventFactory;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentFactory;
import marketdata.field.Field;

public abstract class AbstractResponseHandler<S extends AbstractDataService> extends Thread {
	@Autowired
	protected InstrumentFactory factory;
	@Autowired
	protected MarketDataEventFactory marketDataEventFactory;
	protected S dataService;
	
	public AbstractResponseHandler() {
		
	}
	
	public abstract void setDataService(S dataService);
	public S getDataService() {
		return this.dataService;
	}
	
	public void logDataPointException(Field field,IInstrument instrument) {
		Logger.getRootLogger().log(Level.WARN, this.dataService.getServiceName().name() + " could no process " + field.name() + " for " + instrument.toString());
	}
}
