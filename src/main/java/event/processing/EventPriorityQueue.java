package event.processing;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import event.events.Event;

@Service
@Scope("singleton")
public class EventPriorityQueue extends PriorityBlockingQueue<Event> {

	@Autowired
	private ApplicationEventPublisher publisher;
	
	public EventPriorityQueue() {
		super(200000,new EventInstantComparator());
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

