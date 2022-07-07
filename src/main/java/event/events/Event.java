package event.events;

import java.time.Instant;

import org.springframework.context.ApplicationEvent;

import event.sequencing.processing.EventType;

public abstract class Event extends ApplicationEvent {
	
	private Instant eventTimestamp;
	private EventType eventType;
	private int priority = 10;
	
	public Event(Instant eventTimestamp, EventType eventType) {
		super(eventType);
		this.eventTimestamp = eventTimestamp;
		this.eventType = eventType;
	}

	public Instant getEventTimestamp() {
		return eventTimestamp;
	}

	public void setEventTimestamp(Instant eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	

}
