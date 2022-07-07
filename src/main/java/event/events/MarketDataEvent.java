package event.events;

import java.time.Instant;
import java.time.temporal.Temporal;

import org.springframework.core.Ordered;

import event.sequencing.processing.CoreEventType;
import finance.instruments.IInstrument;
import marketdata.field.Field;
import marketdata.services.base.DataServiceEnum;

public abstract class MarketDataEvent<MarketDataContainer> extends Event {
	protected DataServiceEnum dataService;
	protected Temporal marketDataStartTimestamp;
	protected Temporal marketDataEndTimestamp;
	protected IInstrument instrument;
	protected Field field;
	protected Object value;

	public MarketDataEvent(
			Instant eventTimestamp, 
			CoreEventType eventType,
			DataServiceEnum dataService,
			Temporal marketDataStart,
			Temporal marketDataEnd,
			IInstrument instrument,
			Field field,
			Object value) {
		super(eventTimestamp, eventType);
		this.instrument = instrument;
		this.dataService = dataService;
		this.marketDataStartTimestamp = marketDataStart;
		this.marketDataEndTimestamp = marketDataEnd;
		this.field = field;
		this.value = value;
		this.setPriority(Ordered.HIGHEST_PRECEDENCE);
	}

	public DataServiceEnum getDataService() {
		return dataService;
	}

	public void setDataService(DataServiceEnum dataService) {
		this.dataService = dataService;
	}

	public Temporal getMarketDataStartTimestamp() {
		return marketDataStartTimestamp;
	}

	public void setMarketDataStartTimestamp(Temporal marketDataStartTimestamp) {
		this.marketDataStartTimestamp = marketDataStartTimestamp;
	}

	public Temporal getMarketDataEndTimestamp() {
		return marketDataEndTimestamp;
	}

	public void setMarketDataEndTimestamp(Temporal marketDataEndTimestamp) {
		this.marketDataEndTimestamp = marketDataEndTimestamp;
	}

	public IInstrument getInstrument() {
		return instrument;
	}

	public void setInstrument(IInstrument instrument) {
		this.instrument = instrument;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
}
