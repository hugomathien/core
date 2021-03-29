package marketdata.container;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Tick extends AbstractMarketDataContainer {
	private Instant timestamp;
	
	public Tick(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}
	
	public String toString() {
		String str = "dt: " + this.getTimestamp().toString();
		return str;
	}
}
