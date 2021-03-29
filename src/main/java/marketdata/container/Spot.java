package marketdata.container;

import java.time.Instant;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import exceptions.MarketDataMissingException;
import marketdata.field.Field;

@Component
@Scope("prototype")
public class Spot extends AbstractMarketDataContainer {

	public Spot() {
		
	}
	
	public Instant getTimestamp(Field field) throws MarketDataMissingException {
		if(this.getTimeUpdateMap().containsKey(field))
			return this.getTimeUpdateMap().get(field);
		else
			throw new MarketDataMissingException("Spot data timestamp missing " + field.toString());
	}
}
