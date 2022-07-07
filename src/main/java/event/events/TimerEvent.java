package event.events;

import java.time.Instant;

import org.springframework.core.Ordered;

import event.sequencing.processing.EventType;
import event.sequencing.AbstractEventSequencer;
import event.sequencing.Sequenceable;

public class TimerEvent extends Event {

	private AbstractEventSequencer<?> eventSequencer;
	private Sequenceable sequenceable;
	
	public TimerEvent(AbstractEventSequencer<?> eventSequencer, Instant eventTimestamp,Sequenceable sequenceable,EventType eventType,int priority) {
		super(eventTimestamp, eventType);
		this.eventSequencer = eventSequencer;
		this.sequenceable = sequenceable;
		this.setPriority(priority);
	}
	
	public TimerEvent(AbstractEventSequencer<?> eventSequencer, Instant eventTimestamp,Sequenceable sequenceable,EventType eventType) {
		super(eventTimestamp, eventType);
		this.eventSequencer = eventSequencer;
		this.sequenceable = sequenceable;
		this.setPriority(Ordered.LOWEST_PRECEDENCE);
	}

	public AbstractEventSequencer<?> getEventSequencer() {
		return eventSequencer;
	}

	public void setEventSequencer(AbstractEventSequencer<?> eventSequencer) {
		this.eventSequencer = eventSequencer;
	}

	public Sequenceable getSequenceable() {
		return sequenceable;
	}

	public void setSequenceable(Sequenceable sequenceable) {
		this.sequenceable = sequenceable;
	}

}
