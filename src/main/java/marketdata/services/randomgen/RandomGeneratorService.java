package marketdata.services.randomgen;

import exceptions.DataServiceStartException;
import marketdata.services.base.AbstractDataService;
import marketdata.services.base.DataServiceEnum;

public abstract class RandomGeneratorService extends AbstractDataService {

	public RandomGeneratorService() {
		super(DataServiceEnum.RANDOMGEN);
	}
	
	@Override
	public void init() {
		
	}
	
	public synchronized boolean isOpened() {
		return this.isOpened();
	}
	
	public synchronized void setOpened(boolean opened) {
		this.opened = opened;
		synchronized(this) {
			this.notifyAll();
		}
	}
	
	public synchronized void start() throws DataServiceStartException {
		try {
			if(this.isOpened())
				return;
			this.setOpened(true);
		} catch (Exception e) {
			throw new DataServiceStartException("Random Generator Service failed to start", e);
		}
		
	}
}
