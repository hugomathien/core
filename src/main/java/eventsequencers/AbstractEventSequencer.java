package eventsequencers;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import javax.annotation.PostConstruct;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.format.annotation.DateTimeFormat;

import config.CoreConfig;
import eventprocessors.EventPriorityQueue;
import events.TimerEvent;
import utils.MiscUtils;

public abstract class AbstractEventSequencer extends java.util.Observable {
	@Autowired
	protected EventPriorityQueue queue;
	@Autowired
	protected Clock clock;
	protected ZoneId zoneId = CoreConfig.GLOBAL_ZONE_ID;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	protected LocalDate startDate = CoreConfig.GLOBAL_START_DATE;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	protected LocalDate endDate = CoreConfig.GLOBAL_END_DATE;
	@DateTimeFormat(pattern = "hh:mm:ss")
	protected LocalTime startTime;
	@DateTimeFormat(pattern = "hh:mm:ss")
	protected LocalTime endTime;
	protected Duration step = Duration.ofDays(1);
	protected Duration initialWindowLookBack = Duration.ZERO;
	protected Duration windowLookBack = Duration.ZERO;
	protected Duration windowLookForward = Duration.ZERO;
	private int priority = Ordered.LOWEST_PRECEDENCE;

	public AbstractEventSequencer() {
		this.zoneId = CoreConfig.GLOBAL_ZONE_ID;
		this.startDate = CoreConfig.GLOBAL_START_DATE;
		this.endDate = CoreConfig.GLOBAL_END_DATE;
	}


	public AbstractEventSequencer(Builder builder) {
		this.zoneId = builder.zoneId;
		this.startDate = builder.startDate;
		this.endDate = builder.endDate;
		this.startTime = builder.startTime;
		this.endTime = builder.endTime;
		this.step = builder.step;
		this.windowLookBack = builder.windowLookBack;
		this.windowLookForward = builder.windowLookForward;
		this.initialWindowLookBack = builder.initialWindowLookBack;
	}

	@PostConstruct
	public void initTime() {
		if(startDate == null)
			return;
		else if(endDate == null)
			this.endDate=startDate;

		startDate = MiscUtils.weekday(startDate);
		endDate = MiscUtils.weekday(endDate);
		
		ZonedDateTime startZdt = null;
		ZonedDateTime endZdt = null;
		if(startTime == null)
			startZdt = startDate.atStartOfDay(zoneId);
		else
			startZdt = startDate.atTime(startTime).atZone(zoneId);
		if(endTime == null)
			startZdt = startDate.atStartOfDay(zoneId);
		else
			startZdt = startDate.atTime(endTime).atZone(zoneId);

		//startZdt = BartDateUtils.weekday(startZdt);
		//endZdt = BartDateUtils.weekday(endZdt);
		this.initialWindowLookBack = initialWindowLookBack.compareTo(windowLookBack) > 0 ? initialWindowLookBack : windowLookBack;
		this.clock.setStart(startZdt);
		this.clock.setEnd(endZdt);
		this.clock.setNow(startZdt);
		this.clock.setStartTime(startTime);
		this.clock.setEndTime(endTime);
		this.clock.setZoneId(zoneId);
		this.nextEvent();
	}

	public abstract TimerEvent createEvent(Instant eventTimestamp,Temporal start,Temporal end);

	public void nextEvent() {
		if(!this.clock.elapsed()) {
			Temporal windowStart = null;
			Temporal windowEnd = null;

			LocalDate windowStartDate = this.clock.getNow().minusSeconds((this.clock.isStarted() ? windowLookBack.getSeconds() : initialWindowLookBack.getSeconds())).toLocalDate();
			LocalDate windowEndDate = this.clock.getNow().plusSeconds(windowLookForward.getSeconds()).toLocalDate();
			windowEndDate = (this.clock.getEnd().toLocalDate().isBefore(windowEndDate)) ? this.clock.getEnd().toLocalDate() : windowEndDate;
			windowStart = (this.startTime != null) ? windowStartDate.atTime(startTime).atZone(zoneId) : windowStartDate;
			windowEnd = (this.endTime != null) ? windowEndDate.atTime(endTime).atZone(zoneId) : windowEndDate;

			TimerEvent evt = this.createEvent(clock.getNow().toInstant(), windowStart, windowEnd);
			evt.setPriority(this.getPriority());
			queue.add(evt);
			clock.move(step);
		}
	}
	
	public void processEvent(TimerEvent event) {
		try {
			event.getSequenceable().execute(event.getEventTimestamp());
		}
		catch(Exception e) {
			Logger.getRootLogger().log(Level.WARN, "Failed to run time " + this.getClass().getName() + " @" + event.getEventTimestamp().toString());
		}
	}
	
	public EventPriorityQueue getQueue() {
		return queue;
	}

	public void setQueue(EventPriorityQueue queue) {
		this.queue = queue;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriorirty(int priority) {
		this.priority = priority;
	}
	
	public ZoneId getZoneId() {
		return zoneId;
	}
	
	public void setZoneId(String zoneId) {
		this.zoneId = ZoneId.of(zoneId);
	}
	
	public LocalDate getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = LocalDate.parse(startDate);
	}
	
	public LocalDate getEndDate() {
		return endDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = LocalDate.parse(endDate);
	}
	
	public LocalTime getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = LocalTime.parse(startTime);
		this.clock.setStartTime(this.startTime);
	}
	
	public LocalTime getEndTime() {
		return endTime;
	}
	
	public void setEndTime(String endTime) {
		this.endTime = LocalTime.parse(endTime);
		this.clock.setEndTime(this.endTime);
	}
	
	public Duration getStep() {
		return step;
	}
	
	public void setStep(String step) {
		this.step = Duration.parse(step);
	}
	
	public Duration getWindowLookBack() {
		return windowLookBack;
	}
	
	public void setWindowLookBack(String windowLookBack) {
		this.windowLookBack = Duration.parse(windowLookBack);
	}
	
	public Duration getInitialWindowLookBack() {
		return initialWindowLookBack;
	}
	
	public void setInitialWindowLookBack(String initialWindowLookBack) {
		this.initialWindowLookBack = Duration.parse(initialWindowLookBack);
	}
	
	public Clock getClock() {
		return clock;
	}
	
	public void setClock(Clock clock) {
		this.clock = clock;
	}
	
	public static abstract class Builder {
		private ZoneId zoneId;
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate startDate;
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate endDate;
		@DateTimeFormat(pattern = "hh-mm-ss")
		private LocalTime startTime;
		@DateTimeFormat(pattern = "hh-mm-ss")
		private LocalTime endTime;
		private Duration step = Duration.ofDays(1);
		private Duration initialWindowLookBack = Duration.ZERO;
		private Duration windowLookBack = Duration.ZERO;
		private Duration windowLookForward = Duration.ZERO;

		public Builder() {
			this.zoneId = CoreConfig.GLOBAL_ZONE_ID;
			this.startDate = CoreConfig.GLOBAL_START_DATE;
			this.endDate = CoreConfig.GLOBAL_END_DATE;
		}
		
		public AbstractEventSequencer.Builder zoneId(ZoneId zoneId) {
			this.zoneId = zoneId;
			return this;
		}
		
		public AbstractEventSequencer.Builder date(LocalDate date) {
			this.startDate = date;
			this.endDate = date;
			return this;
		}
		
		public AbstractEventSequencer.Builder startDate(LocalDate startDate) {
			this.startDate = startDate;
			return this;
		}
		
		public AbstractEventSequencer.Builder endDate(LocalDate endDate) {
			this.endDate = endDate;
			return this;
		}
		
		public AbstractEventSequencer.Builder startTime(LocalTime startTime) {
			this.startTime = startTime;
			return this;
		}
		
		public AbstractEventSequencer.Builder endTime(LocalTime endTime) {
			this.endTime = endTime;
			return this;
		}
		
		public AbstractEventSequencer.Builder startInstant(Instant instant) {
			this.startDate = instant.atZone(this.zoneId).toLocalDate();
			this.startTime = instant.atZone(this.zoneId).toLocalTime();
			return this;
		}

		public AbstractEventSequencer.Builder endInstant(Instant instant) {
			this.endDate = instant.atZone(this.zoneId).toLocalDate();
			this.endTime = instant.atZone(this.zoneId).toLocalTime();
			return this;
		}
		
		public AbstractEventSequencer.Builder step(Duration step) {
			this.step = step;
			return this;
		}
		
		public AbstractEventSequencer.Builder windowLookBack(Duration windowLookBack) {
			this.windowLookBack = windowLookBack;
			return this;
		}
		
		public AbstractEventSequencer.Builder windowLookForward(Duration windowLookForward) {
			this.windowLookForward = windowLookForward;
			return this;
		}
		
		public AbstractEventSequencer.Builder initialWindowLookBack(Duration initialWindowLookBack) {
			this.initialWindowLookBack = initialWindowLookBack;
			return this;
		}
		
		public abstract AbstractEventSequencer build();
	}
}


