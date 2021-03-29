package eventprocessors;

import java.time.Instant;
import java.time.LocalDate;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import events.Event;
import events.InstrumentDelistEvent;
import events.InstrumentUpdateEvent;
import events.MarketDataEventBar;
import events.MarketDataEventDay;
import events.MarketDataEventSpot;
import events.MarketDataEventTick;
import events.NewInstrumentEvent;
import events.PortfolioCompositionEvent;
import events.TimerEvent;
import events.TradingSessionEvent;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.instruments.InstrumentFactory;
import marketdata.HistoricalData;

@Service
@Scope("singleton")
@Lazy(false)
public class CoreEventListeners {
	@Autowired
	private InstrumentFactory instrumentFactory;
	
	public CoreEventListeners() {}
	
	
	@EventListener
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void logEvent(Event event) {
		Logger.getRootLogger().log(Level.INFO, "Received @"+ event.toString());
	}
	
	@EventListener(condition = "#event.eventType.name() == 'BAR'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void bar(MarketDataEventBar event) {
		if(event.getField().valueIsValid(event.getValue())) {
			HistoricalData historicalData = event.getInstrument().getMarketData().getHistorical();
			historicalData.getBarData().getBar((Instant) event.getMarketDataEndTimestamp(), (Instant) event.getMarketDataEndTimestamp()).getFieldsMap().put(event.getField(),event.getValue());
		}
	}
	
	@EventListener(condition = "#event.eventType.name() == 'TICK'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void tick(MarketDataEventTick event) {
		if(event.getField().valueIsValid(event.getValue())) {
			HistoricalData historicalData = event.getInstrument().getMarketData().getHistorical();
			historicalData.getTickData().getTick((Instant) event.getMarketDataEndTimestamp()).put(event.getField(), event.getValue());
		}
	}
	
	@EventListener(condition = "#event.eventType.name() == 'DAY'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void bar(MarketDataEventDay event) {
		if(event.getField().valueIsValid(event.getValue())) {
			HistoricalData historicalData = event.getInstrument().getMarketData().getHistorical();
			historicalData.getEodData().getEod((LocalDate) event.getMarketDataEndTimestamp()).put(event.getField(), event.getValue());
		}
	}
	
	@EventListener(condition = "#event.eventType.name() == 'SPOT'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void spot(MarketDataEventSpot event) {
		if(event.getField().valueIsValid(event.getValue())) {
			event.getInstrument().getMarketData().getSpot().put(event.getField(), event.getValue());
			event.getInstrument().getMarketData().getSpot().putTimestamp(event.getField(),event.getEventTimestamp());
		}
	}
	
	@EventListener(condition = "#event.eventType.name() == 'NEW_INSTRUMENT'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void newInstrument(NewInstrumentEvent event) {
		// TODO: instrument creation occurs wihtin the data response handler
	}
	
	@EventListener(condition = "#event.eventType.name() == 'UPDATE_ISNTRUMENT' && event.instrumentType.name() == 'Future'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void createActiveFuture(InstrumentUpdateEvent event) {
		//instrumentFactory.makeActiveFuture((Future) event.getInstrument(), event.getEventTimestamp().atZone(ZoneId.systemDefault()).toLocalDate());
	}
	
	@EventListener(condition = "#event.eventType.name() == 'DELIST_INSTRUMENT'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void delistInstrument(InstrumentDelistEvent event) {
	}
	
	@EventListener(condition = "#event.eventType.name() == 'PORTFOLIO_COMPOSITION'")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void portfolioComposition(PortfolioCompositionEvent event) {
		event.getPortfolio().addMember(event.getMember(),event.getWeight());
	}
	
	@EventListener
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void tradingSession(TradingSessionEvent event) {
		event.getExchange().setLastSessionEvent(event);
	}

	@EventListener
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public void timerEvent(TimerEvent event) throws DataQueryException,DataServiceStartException {
		event.getEventSequencer().processEvent(event);
	}
	
}

