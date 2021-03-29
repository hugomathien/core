package events;

import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eventprocessors.CoreEventType;
import finance.springBean.Exchange;
import finance.springBean.TradingSession;

@Component
@Scope("prototype")
@Lazy(true)
public class TradingSessionStartEvent extends TradingSessionEvent {

	public TradingSessionStartEvent(Instant eventTimestamp,Exchange exchange,TradingSession session) {
		super(eventTimestamp,CoreEventType.TRADING_SESSION_START,exchange,session);
		this.setPriority(1);
	}
}
