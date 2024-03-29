package finance.instruments;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import exceptions.MarketDataMissingException;
import finance.identifiers.Identifier;
import finance.identifiers.IdentifierType;
import finance.identifiers.Ric;
import finance.identifiers.Ticker;
import finance.misc.Exchange;
import marketdata.MarketData;
import marketdata.MarketDataFunctions;
import marketdata.container.MarketDataContainerEnum;
import marketdata.field.Field;
import config.CoreConfig;

public abstract class Instrument implements IInstrument {

	private InstrumentType instrumentType;
	private IdentifierType primaryIdentifierType = CoreConfig.PRIMARY_IDENTIFIER_TYPE;
	private final MarketData marketData;
	private final Map<Field,Object> staticData;
	private final Set<Identifier> identifiers;
	private String currency;
	private Exchange exchange; // TODO: only listed instrument can have an exchange
	
	public Instrument() {
		marketData = new MarketData();
		staticData = new HashMap<Field,Object>();
		identifiers = new HashSet<Identifier>();

	}
	
	public Identifier getPrimaryIdentifier() {
		return this.getIdentifier(this.primaryIdentifierType);
	}
	
	public Identifier getIdentifier(String name) {
		return this.getIdentifiers().stream().filter(i -> i.getName().equals(name)).findAny().orElse(null);
	}
	
	public Identifier getIdentifier(IdentifierType type) {
		Identifier returnID = null;
		for(Identifier identifier : this.getIdentifiers()) {
			if(identifier.getType().equals(type)) // TODO: Handling of multiple identifiers of the same type ? Change of tickers etc...
				returnID = (Identifier) identifier;
		}
		
		return returnID;
	}

	public InstrumentType getInstrumentType() {
		return instrumentType;
	}

	public void setInstrumentType(InstrumentType instrumentType) {
		this.instrumentType = instrumentType;
	}

	public IdentifierType getPrimaryIdentifierType() {
		return primaryIdentifierType;
	}

	public void setPrimaryIdentifierType(IdentifierType primaryIdentifierType) {
		this.primaryIdentifierType = primaryIdentifierType;
	}

	public MarketData getMarketData() {
		return marketData;
	}

	public Map<Field, Object> getStaticData() {
		return staticData;
	}

	public Set<Identifier> getIdentifiers() {
		return identifiers;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public String getRic() {
		if(this.getIdentifiers().stream().anyMatch(p -> p instanceof Ric))
			return this.getIdentifier(IdentifierType.RIC).getName();
		else
			return null;
	}
	
	public String getTicker() {
		if(this.getIdentifiers().stream().anyMatch(p -> p instanceof Ticker))
			return this.getIdentifier(IdentifierType.TICKER).getName();
		else
			return null;
	}


	public String replaceIdentifierWithComposite(IdentifierType idType,String id) {
		return this.instrumentType.replaceIdentifierWithComposite(idType,id);
	}

	private Exchange guessExchange() {
		Exchange exchange = null;
		for(Identifier identifier : this.getIdentifiers()) {
			exchange = this.instrumentType.guessExchange(identifier.getType(),identifier.getName());
			if(exchange != null)
				break;
		}
		
		return exchange;
	}
	
	public Exchange getExchange() {
		if(exchange == null)
			exchange = guessExchange();
		
		return exchange;
	}

	public FX getFX(String rightCurrency) {
		FX fx = CoreConfig.services().getOrMakeFx(this.currency,currency);
		return fx;
	}
	
	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}
	
	public static abstract class Builder<K extends Instrument> {
		protected LinkedHashMap<IdentifierType,String> identifiers;
		
		public Builder() {
			this.identifiers = new LinkedHashMap<IdentifierType,String>();
		}
		
		public Builder<K> ticker(String ticker) {
			this.identifiers.put(IdentifierType.TICKER, ticker);
			return this;
		}
		
		public Builder<K> ric(String ric) {
			this.identifiers.put(IdentifierType.RIC, ric);
			return this;
		}
		
		public Builder<K> sedol(String sedol) {
			this.identifiers.put(IdentifierType.SEDOL, sedol);
			return this;
		}
		
		public abstract K build();
	}
	
	public Object getSpot(Field field) throws MarketDataMissingException {
		try {
			return this.getMarketData().getSpot().get(field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Spot data missing " + field.toString() + " " + this.getPrimaryIdentifier().toString() + " Error: " + e);
		}
	}
	
	public Object getTick(ZonedDateTime datetime,Field field) throws MarketDataMissingException {
		try {
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.getMarketDataContainer.apply(
							MarketDataFunctions.getTickTimeSeries.apply(this), datetime.toInstant()),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Tick data  missing" + field.toString() + " " + this.getPrimaryIdentifier().toString() + " Error: " + e);
		}
	}
	
	public Object getTickCeil(ZonedDateTime datetime,Field field) throws MarketDataMissingException {
		try {
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.ceilMarketDataContainer.apply(
							MarketDataFunctions.isNotMissing.apply(
									MarketDataFunctions.getTickTimeSeries.apply(this),field), datetime.toInstant()),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Tick data ceil missing" + field.toString() + " " + this.getPrimaryIdentifier().toString() + datetime.toString() + " Error: " + e);
		}
	}
	
	public Object getTickFloor(ZonedDateTime datetime,Field field) throws MarketDataMissingException {
		try {
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.floorMarketDataContainer.apply(
							MarketDataFunctions.isNotMissing.apply(
									MarketDataFunctions.getTickTimeSeries.apply(this),field), datetime.toInstant()),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Tick data floor missing" + field.toString() + " " + this.getPrimaryIdentifier().toString() + datetime.toString() + " Error: " + e);
		}
	}

	public Object getBar(ZonedDateTime datetime,Field field) throws MarketDataMissingException {
		return this.getBar(datetime, field,1);
	}
	
	public Object getBarCeil(ZonedDateTime datetime,Field field) throws MarketDataMissingException {
		return this.getBarCeil(datetime, field,1);
	}
	
	public Object getBarFloor(ZonedDateTime datetime,Field field) throws MarketDataMissingException {
		return this.getBarFloor(datetime, field,1);
	}
	
	public Object getBar(ZonedDateTime datetime,Field field, Integer minuteInterval) throws MarketDataMissingException {
		try {
			if(minuteInterval == null)
				minuteInterval = 1;
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.getMarketDataContainer.apply(
							MarketDataFunctions.getBarTimeSeries.apply(this,minuteInterval), datetime.toInstant()),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Bar data  missing" + field.toString() + " " + 
					this.getPrimaryIdentifier().toString() + datetime.toString() + " Error: " + e);
		}
	}
	
	public Object getBarCeil(ZonedDateTime datetime,Field field, Integer minuteInterval) throws MarketDataMissingException {
		try {
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.ceilMarketDataContainer.apply(
							MarketDataFunctions.isNotMissing.apply(
									MarketDataFunctions.getBarTimeSeries.apply(this,minuteInterval),field), datetime.toInstant()),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Bar data ceil missing" + field.toString() + " " + 
					this.getPrimaryIdentifier().toString() + datetime.toString() + " Error: " + e);
		}
	}
	
	public Object getBarFloor(ZonedDateTime datetime,Field field, Integer minuteInterval) throws MarketDataMissingException {
		try {
			
			if(minuteInterval == null)
				minuteInterval = 1;
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.floorMarketDataContainer.apply(
							MarketDataFunctions.isNotMissing.apply(
									MarketDataFunctions.getTickTimeSeries.apply(this),field), datetime.toInstant()),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Bar data floor missing" + field.toString() + " " + 
					this.getPrimaryIdentifier().toString() + datetime.toString() + " Error: " + e);
		}
	}

	public Object getEod(LocalDate date,Field field) throws MarketDataMissingException {
		try {
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.getMarketDataContainer.apply(
							MarketDataFunctions.getEodTimeSeries.apply(this), date),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Eod data missing" + field.toString() + " " + this.getPrimaryIdentifier().toString() + date.toString() + " Error: " + e);
		}
	}
	
	public Object getEodCeil(LocalDate date,Field field) throws MarketDataMissingException {
		try {
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.ceilMarketDataContainer.apply(
							MarketDataFunctions.isNotMissing.apply(
									MarketDataFunctions.getEodTimeSeries.apply(this),field), date),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Eod data ceil missing" + field.toString() + " " + this.getPrimaryIdentifier().toString() + date.toString() + " Error: " + e);
		}
	}
	
	public Object getEodFloor(LocalDate date,Field field) throws MarketDataMissingException {
		try {
			return MarketDataFunctions.getFieldValue.apply(
					MarketDataFunctions.floorMarketDataContainer.apply(
							MarketDataFunctions.isNotMissing.apply(
									MarketDataFunctions.getEodTimeSeries.apply(this),field), date),field);
		}
		catch(Exception e) {
			throw new MarketDataMissingException("Eod data floor missing" + field.toString() + " " + this.getPrimaryIdentifier().toString() + date.toString() + " Error: " + e);
		}
	}
	
	public Object getMarketData(Temporal datetime,Field field,MarketDataContainerEnum dataType) throws MarketDataMissingException {
		return getMarketData(datetime,field,dataType,null);
	}
	
	public Object getMarketDataFloor(Temporal datetime,Field field,MarketDataContainerEnum dataType) throws MarketDataMissingException {
		return getMarketDataFloor(datetime,field,dataType,null);
	}
	
	public Object getMarketDataCeil(Temporal datetime,Field field,MarketDataContainerEnum dataType) throws MarketDataMissingException {
		return getMarketDataCeil(datetime,field,dataType,null);
	}
	
	public Object getMarketData(Temporal datetime,Field field,MarketDataContainerEnum dataType,Integer interval) throws MarketDataMissingException {
		Object value = null;
		switch(dataType) {
		case BAR:
			value = this.getBar((ZonedDateTime) datetime, field, interval);
			break;
		case DAY:
			value = this.getEod((LocalDate) datetime, field);
			break;
		case TICK:
			value = this.getTick((ZonedDateTime) datetime, field);
			break;
		default:
			break;
		}
		return value;
	}
	
	public Object getMarketDataFloor(Temporal datetime,Field field,MarketDataContainerEnum dataType,Integer interval) throws MarketDataMissingException {
		Object value = null;
		switch(dataType) {
		case BAR:
			value = this.getBarFloor((ZonedDateTime) datetime, field, interval);
			break;
		case DAY:
			value = this.getEodFloor((LocalDate) datetime, field);
			break;
		case TICK:
			value = this.getTickFloor((ZonedDateTime) datetime, field);
			break;
		default:
			break;
		}
		return value;
	}
	
	public Object getMarketDataCeil(Temporal datetime,Field field,MarketDataContainerEnum dataType,Integer interval) throws MarketDataMissingException {
		Object value = null;
		switch(dataType) {
		case BAR:
			value = this.getBarCeil((ZonedDateTime) datetime, field, interval);
			break;
		case DAY:
			value = this.getEodCeil((LocalDate) datetime, field);
			break;
		case TICK:
			value = this.getTickCeil((ZonedDateTime) datetime, field);
			break;
		default:
			break;
		}
		return value;
	}
	
	public String toString() {
		return this.getPrimaryIdentifier().toString();
	}
}
