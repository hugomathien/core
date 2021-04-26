package marketdata.services.randomgen;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import marketdata.services.base.AbstractResponseHandler;
import marketdata.services.base.DataRequest;
import marketdata.services.base.IReferenceDataService;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.randomgen.responsehandler.RandomGeneratorReferenceResponseHandler;
import utils.MiscUtils;

@Service("RANDOMGEN_REFERENCE")
@Scope("singleton")
@Lazy(true)
public class RandomGeneratorReferenceDataService extends RandomGeneratorService implements IReferenceDataService<Object> {
	@Autowired
	private RandomGeneratorReferenceResponseHandler responseHandler;
	// for HistoricalDataRequest loop weekday from start to end date 
	// inner loop on instruments/fields to generate a random value. 
	// Publish to mkt data event factory 

	public RandomGeneratorReferenceDataService() {
		super();
	}
	
	public Object query(DataRequest<Object> requestBuilder, RequestType requestType)  throws DataQueryException {
		switch(requestType) {
		case ReferenceDataRequest:
		case HistoricalDataRequest:
			this.historicalDataRequest(requestBuilder);
			break;
		default:
			break;
		}
		return null;
	}
	
	public void historicalDataRequest(DataRequest<Object> requestBuilder) {
		LocalDate sd = (LocalDate) requestBuilder.getParameters().get(RequestParameters.startDate);
		LocalDate ed = (LocalDate) requestBuilder.getParameters().get(RequestParameters.endDate);

		LocalDate ld = MiscUtils.weekday(sd);
		while(!ld.isAfter(ed)) {
			
			this.responseHandler.historicalDataRequest(requestBuilder,ld);
			ld = MiscUtils.plusWeekdays(1, ld);
		}
		
		
	}
	
	
	public AbstractResponseHandler<?> getResponseHandler() {
		return this.getResponseHandler();
	}
	
	public void setResponseHandler(RandomGeneratorReferenceResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	@Override
	public boolean isOpened() {
		return this.opened;
	}

	@Override
	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	@Override
	public void start() throws DataServiceStartException {
		if(this.isOpened())
			return;
		this.setOpened(true);
	}

	@Override
	public RequestType[] getRequestDiscovery() {
		return this.requestDiscovery;
	}

	@Override
	@Value("${randomgen.requestTypes}")
	public void setRequestDiscovery(RequestType[] requestDiscovery) {
		this.requestDiscovery = requestDiscovery;
	}
	
}
