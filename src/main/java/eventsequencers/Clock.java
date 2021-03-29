package eventsequencers;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import utils.MiscUtils;

@Component
@Scope("prototype")
@Lazy(true)
public class Clock {
	private ZoneId zoneId;
	private LocalTime startTime;
	private LocalTime endTime;
	private ZonedDateTime start;
	private ZonedDateTime end;
	private ZonedDateTime now;
	private boolean started;
	
	public Clock() {
		started = false;
	}

	public ZoneId getZoneId() {
		return zoneId;
	}

	public void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public ZonedDateTime getStart() {
		return start;
	}

	public void setStart(ZonedDateTime start) {
		this.start = start;
	}

	public ZonedDateTime getEnd() {
		return end;
	}

	public void setEnd(ZonedDateTime end) {
		this.end = end;
	}

	public ZonedDateTime getNow() {
		return now;
	}

	public void setNow(ZonedDateTime now) {
		this.now = now;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
	
	public boolean elapsed() {
		return this.now.isAfter(this.end);
	}
	
	public void terminate() {
		this.setNow(this.end.plusMinutes(1));
	}
	
	public void move(Duration step) {
		ZonedDateTime newnow = now.plusSeconds(step.getSeconds());
		newnow = MiscUtils.weekday(newnow);
		if(this.endTime!=null && this.now.toLocalDate().atTime(this.endTime).atZone(zoneId).isBefore(newnow) && this.now.toLocalDate().equals(newnow.toLocalDate()))
			newnow = MiscUtils.plusWeekdays(1, this.now.toLocalDate()).atTime(this.startTime).atZone(zoneId); // TODO check if should be zoneId or systemDefault ?
	
		this.now = newnow;
		this.started = true;
	}
}
