package event.timers;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import utils.MiscUtils;


public class Clock {
	private ZoneId zoneId;
	private LocalTime startTime;
	private LocalTime endTime;
	private ZonedDateTime start;
	private ZonedDateTime end;
	private ZonedDateTime now;
	private boolean started;
	private boolean runOnceOnTermination;
	private boolean includeWeekend;
	public Clock() {
		started = false;
		runOnceOnTermination = false;
		includeWeekend = false;
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

	public boolean isRunOnceOnTermination() {
		return runOnceOnTermination;
	}

	public void setRunOnceOnTermination(boolean runOnceOnTermination) {
		this.runOnceOnTermination = runOnceOnTermination;
	}

	public boolean isIncludeWeekend() {
		return includeWeekend;
	}

	public void setIncludeWeekend(boolean includeWeekend) {
		this.includeWeekend = includeWeekend;
	}

	public boolean elapsed() {
		return this.now.isAfter(this.end);
	}
	
	public void terminate() {
		this.setNow(this.end.plusMinutes(1));
	}

	public void move(Duration step) {
		ZonedDateTime newnow = now.plusSeconds(step.getSeconds());
		if(this.isRunOnceOnTermination() && newnow.isAfter(this.end)) {
			if (this.now.isBefore(this.end))  // we are moving past the end time
				newnow = this.end; //TODO: if end is a weekend the newnow will default to next business day so the clock will terminate
		}

		if(!this.includeWeekend)
			newnow = MiscUtils.weekday(newnow);

		if(this.endTime!=null && this.now.toLocalDate().atTime(this.endTime).atZone(zoneId).isBefore(newnow) && this.now.toLocalDate().equals(newnow.toLocalDate())) {
			if(!this.includeWeekend)
				newnow = MiscUtils.plusWeekdays(1, this.now.toLocalDate()).atTime(this.startTime).atZone(zoneId);
			else
				newnow = this.now.toLocalDate().plusDays(1).atTime(this.startTime).atZone(zoneId);
		}
		this.now = newnow;
		this.started = true;
	}
}
