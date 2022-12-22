package marketdata.services.base;

import javax.annotation.PostConstruct;

import exceptions.DataQueryException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import exceptions.DataServiceStartException;
import finance.instruments.IInstrument;
import marketdata.field.Field;

public abstract class AbstractDataService implements IDataService {
	protected volatile boolean opened = false;
	protected RequestType[] requestDiscovery;
	protected DataServiceEnum serviceName;
	
	public AbstractDataService(DataServiceEnum serviceName) {
		this.serviceName = serviceName;
	}
	
	@PostConstruct
	public abstract void init();
	
	public synchronized void setOpened(boolean opened) {
		this.opened = opened;
		synchronized(this) {
			this.notifyAll();
		}
	}
	
	public abstract void start() throws DataServiceStartException;
	
	public RequestType[] getRequestDiscovery() {
		return requestDiscovery;
	}

	public DataServiceEnum getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(DataServiceEnum serviceName) {
		this.serviceName = serviceName;
	}
	
	public void logDataResponseException(Field field, IInstrument instrument) {
		Logger.getRootLogger().log(Level.WARN, this.getServiceName().name() + " could not process " + field.name() + " for " + instrument.getPrimaryIdentifier().toString());;
	}

	public abstract void query(DataRequest requestBuilder)  throws DataQueryException;
}
