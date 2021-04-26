package marketdata.services.randomgen.responsehandler;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import config.CoreConfig;
import event.events.MarketDataEventFactory;
import event.processing.EventPriorityQueue;
import finance.identifiers.Identifier;
import finance.instruments.IInstrument;
import finance.instruments.Instrument;
import marketdata.container.MarketDataContainerEnum;
import marketdata.field.Field;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.bloomberg.responsehandler.BBGReferenceResponseHandler;

@Service
@Scope("singleton")
@Lazy
public class RandomGeneratorReferenceResponseHandler {
	@Autowired
	private BBGReferenceResponseHandler responseHandler;
	@Autowired
	private MarketDataEventFactory marketDataEventFactory;
	@Autowired
	private EventPriorityQueue queue;

	public RandomGeneratorReferenceResponseHandler() {}
	
	public void historicalDataRequest(DataRequest<Object> request,LocalDate ld) {
		double scales[] = new double [request.getFields().size()];
		if(request.getParameters().containsKey(RequestParameters.randomizedNumberScales))
			scales = (double[]) request.getParameters().get(RequestParameters.randomizedNumberScales);
		else
			Arrays.fill(scales,1.0);	

		Iterator<Identifier> idit = request.getIdentifiers().iterator();
		while(idit.hasNext()) {
			IInstrument instrument = idit.next().getInstrument();
			int i =0;
			for(Field field : request.getFields()) {
				Instant timestamp;
				try {
					timestamp = field.assignTimeStamp(ld,((Instrument) instrument).getExchange()).toInstant();
				}
				catch(Exception e) {
					timestamp = ld.atStartOfDay(CoreConfig.GLOBAL_ZONE_ID).toInstant();
				}
				
				double scale = scales[i];
				Object val = this.randomize(field,scale);
				
				marketDataEventFactory.publishToEventQueue(
						request.isBackfill(), 
						timestamp, 
						MarketDataContainerEnum.DAY, 
						DataServiceEnum.RANDOMGEN, 
						timestamp,  
						timestamp,  
						instrument, 
						field, 
						val);
				i++;
			}
		}		
	}
	
	public Object randomize(Field field,double scale) {				
		double rnd = Precision.round(Math.random() * scale,4);
		return field.cast(rnd);
	}
	
	
}
