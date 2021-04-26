package events;

import java.time.Instant;

import eventprocessors.EventType;
import finance.springBean.Exchange;
import finance.springBean.TradingSession;

public abstract class TradingSessionEvent extends Event {
	private Exchange exchange;
	private TradingSession session;
	
	public TradingSessionEvent(Instant eventTimestamp,EventType type,Exchange exchange,TradingSession session) {
		super(eventTimestamp,type);
		this.exchange = exchange;
		this.session = session;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	public TradingSession getSession() {
		return session;
	}

	public void setSession(TradingSession session) {
		this.session = session;
	}
	
}
