package events;

import java.time.Instant;
import java.time.temporal.Temporal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import eventprocessors.EventPriorityQueue;
import finance.instruments.IInstrument;
import marketdata.container.MarketDataContainerEnum;
import marketdata.field.Field;
import marketdata.services.base.DataServiceEnum;

@Service
@Scope("singleton")
@Lazy(true)
public class MarketDataEventFactory {
	@Autowired
	private EventPriorityQueue queue;

	public MarketDataEventFactory() {}

	public void publishToEventQueue(
		boolean isBackfill,
		Instant eventTimestamp,
		MarketDataContainerEnum dataContainerType,
		DataServiceEnum dataService,
		Temporal marketDataStart,
		Temporal marketDataEnd,
		IInstrument instrument,
		Field field,
		Object value) {
			
		if(!isBackfill) {
				queue.add(new MarketDataEventSpot(eventTimestamp,dataService,marketDataEnd,marketDataEnd,instrument,field,value));
				return;
		}
		
		switch(dataContainerType) {
			case BAR:
				queue.add(new MarketDataEventBar(eventTimestamp, dataService, marketDataStart,marketDataEnd,instrument,field,value));
				break;
			case DAY:
				queue.add(new MarketDataEventBar(eventTimestamp, dataService, marketDataStart,marketDataEnd,instrument,field,value));
				break;
			case TICK:
				queue.add(new MarketDataEventBar(eventTimestamp, dataService, marketDataStart,marketDataEnd,instrument,field,value));
				break;
			case SPOT:
				queue.add(new MarketDataEventBar(eventTimestamp, dataService, marketDataStart,marketDataEnd,instrument,field,value));
				break;
			default:
				return;
		}
	}

}
