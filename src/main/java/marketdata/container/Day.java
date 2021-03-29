package marketdata.container;

import java.time.LocalDate;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Day extends AbstractMarketDataContainer {
	private LocalDate date;
	
	public Day(LocalDate date) {
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public String toString() {
		String str = "dt: " + this.getDate().toString();
		return str;
	}
	
	
}
