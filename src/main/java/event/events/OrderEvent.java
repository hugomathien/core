package event.events;

import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import event.processing.CoreEventType;
import finance.order.Order;

@Component
@Scope("prototype")
@Lazy(true)
public class OrderEvent extends Event {
	private Order order;
	
	public OrderEvent(Instant eventTimestamp,Order order) {
		super(eventTimestamp,CoreEventType.CLIENT_ORDER);
		this.order = order;
		this.setPriority(Ordered.HIGHEST_PRECEDENCE);
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	
}
