package event.events;

import java.time.Instant;
import java.time.temporal.Temporal;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import event.processing.CoreEventType;
import finance.instruments.IInstrument;
import marketdata.container.Bar;
import marketdata.field.Field;
import marketdata.services.base.DataServiceEnum;

@Component
@Scope("prototype")
@Lazy(true)
public class MarketDataEventBar extends MarketDataEvent<Bar> {

	public MarketDataEventBar(
			Instant eventTimestamp,
			DataServiceEnum dataService,
			Temporal marketDataStart,
			Temporal marketDataEnd,
			IInstrument instrument,
			Field field,
			Object value) {
		super(eventTimestamp, CoreEventType.BAR, dataService, marketDataStart, marketDataEnd, instrument, field, value);
	}
			
	
	
}
