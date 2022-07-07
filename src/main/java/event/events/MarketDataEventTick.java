package event.events;

import java.time.Instant;
import java.time.temporal.Temporal;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import event.sequencing.processing.CoreEventType;
import finance.instruments.IInstrument;
import marketdata.container.Tick;
import marketdata.field.Field;
import marketdata.services.base.DataServiceEnum;

@Component
@Scope("prototype")
@Lazy(true)
public class MarketDataEventTick extends MarketDataEvent<Tick> {

	public MarketDataEventTick(
			Instant eventTimestamp,
			DataServiceEnum dataService,
			Temporal marketDataStart,
			Temporal marketDataEnd,
			IInstrument instrument,
			Field field,
			Object value) {
		super(eventTimestamp, CoreEventType.TICK, dataService, marketDataStart, marketDataEnd, instrument, field, value);
	}
			
	
	
}
