package event.processing;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import event.events.Event;

@Service
@Scope("singleton")
public class EventPriorityQueue extends PriorityQueue<Event> {

	@Autowired
	private ApplicationEventPublisher publisher;
	
	public EventPriorityQueue() {
		super(11,new EventInstantComparator());
	}
	
	public Event pollAndPublish() {
		Event ev = this.poll();
		publisher.publishEvent(ev);
		return ev;
	}
	
	public void pollAndPublishAll() {
		while(!this.isEmpty()) {
			Event ev = this.poll();
			publisher.publishEvent(ev);
		}
	}
}

class EventInstantComparator implements Comparator<Event> {
	@Override
	public int compare(Event e1, Event e2) {
		int comp=e1.getEventTimestamp().compareTo(e2.getEventTimestamp());
		
		if(comp==0) {
			if(e1.getPriority()<e2.getPriority())
				return -1;
			else if(e1.getPriority()>e2.getPriority())
				return 1;
			else
				return 0;
		}
		return comp;
	}
}
