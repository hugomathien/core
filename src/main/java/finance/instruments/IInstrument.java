package finance.instruments;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

import exceptions.MarketDataMissingException;
import finance.identifiers.IIdentifier;
import finance.identifiers.Identifier;
import finance.identifiers.IdentifierType;
import marketdata.MarketData;
import marketdata.container.MarketDataContainerEnum;
import marketdata.field.Field;

public interface IInstrument {
	public List<IIdentifier> getIdentifiers();
	public Identifier getIdentifier(IdentifierType type);
	public Map<Field,Object> getStaticData();
	public MarketData getMarketData();
	public Identifier getPrimaryIdentifier();
	public IdentifierType getPrimaryIdentifierType();
	public InstrumentType getInstrumentType();
	public void setPrimaryIdentifierType(IdentifierType identifierType);
	public String getRic();
	public String getTicker();
	public String getCurrency();
	public void setCurrency(String currency);
	public Object getSpot(Field field);
	
	public Object getTick(ZonedDateTime dateTime, Field field) throws MarketDataMissingException;
	public Object getTickCeil(ZonedDateTime dateTime, Field field) throws MarketDataMissingException;
	public Object getTickFloor(ZonedDateTime dateTime, Field field) throws MarketDataMissingException;
	public Object getBar(ZonedDateTime dateTime, Field field) throws MarketDataMissingException;
	public Object getBarCeil(ZonedDateTime dateTime, Field field) throws MarketDataMissingException;
	public Object getBarFloor(ZonedDateTime dateTime, Field field) throws MarketDataMissingException;
	public Object getEod(LocalDate date, Field field) throws MarketDataMissingException;
	public Object getEodCeil(LocalDate date, Field field) throws MarketDataMissingException;
	public Object getEodFloor(LocalDate date, Field field) throws MarketDataMissingException;
	public Object getMarketData(Temporal dateTime, Field field, MarketDataContainerEnum dataType) throws MarketDataMissingException;
	public Object getMarketDataFloor(Temporal dateTime, Field field, MarketDataContainerEnum dataType) throws MarketDataMissingException;
	public Object getMarketDataCeil(Temporal dateTime, Field field, MarketDataContainerEnum dataType) throws MarketDataMissingException;
	public Object getMarketData(Temporal dateTime, Field field, MarketDataContainerEnum dataType,Integer interval) throws MarketDataMissingException;
	public Object getMarketDataFloor(Temporal dateTime, Field field, MarketDataContainerEnum dataType,Integer interval) throws MarketDataMissingException;
	public Object getMarketDataCeil(Temporal dateTime, Field field, MarketDataContainerEnum dataType,Integer interval) throws MarketDataMissingException;

}
