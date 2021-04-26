package marketdata.container;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Day extends AbstractMarketDataContainer {
	private Instant date;
	
	public Day(Instant date) {
		this.date = date;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("dt: " + this.getDate().toString());
		str.append("value: " + this.getFieldsMap().toString());
		return str.toString();
	}
	
	
}
