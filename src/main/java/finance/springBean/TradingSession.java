package finance.springBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import finance.tradingcycle.TradingPhaseEnum;

public class TradingSession {
	private TradingPhaseEnum tradingPhase;
	@DateTimeFormat(pattern = "hh:mm:ss")
	private LocalTime start;
	@DateTimeFormat(pattern = "hh:mm:ss")
	private LocalTime end;
	
	public TradingSession (String start, String end) {
		this.start = LocalTime.parse(start);
		this.end = LocalTime.parse(end);
	}

	public TradingPhaseEnum getTradingPhase() {
		return tradingPhase;
	}

	public void setTradingPhase(TradingPhaseEnum tradingPhase) {
		this.tradingPhase = tradingPhase;
	}

	public ZonedDateTime getStart(LocalDate date, ZoneId zoneId) {
		return ZonedDateTime.of(date,this.start, zoneId);
	}
	
	public LocalTime getStart() {
		return start;
	}

	public void setStart(LocalTime start) {
		this.start = start;
	}

	public ZonedDateTime getEnd(LocalDate date, ZoneId zoneId) {
		return ZonedDateTime.of(date,this.end, zoneId);
	}
	
	public LocalTime getEnd() {
		return end;
	}
	
	public void setEnd(LocalTime end) {
		this.end = end;
	}
	
}
