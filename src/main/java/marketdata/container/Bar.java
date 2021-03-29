package marketdata.container;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Bar extends AbstractMarketDataContainer {
	private Instant start;
	private Instant end;

	public Bar(Instant start,Instant end) {
		this.start = start;
		this.end = end;
	}

	public Instant getStart() {
		return start;
	}

	public void setStart(Instant start) {
		this.start = start;
	}

	public Instant getEnd() {
		return end;
	}

	public void setEnd(Instant end) {
		this.end = end;
	}
	
	public String toString() {
		String str = "start: " + this.getStart().toString() + " end: " + this.getEnd().toString();
		return str;
	}
	
	public Duration getDuratipon() {
		return Duration.between(this.getStart(), this.getEnd());
	}
	
}
