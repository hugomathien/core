package marketdata.container;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import exceptions.MarketDataMissingException;
import marketdata.field.Field;

public abstract class AbstractMarketDataContainer {
	private ConcurrentHashMap<Field,Object> fieldsMap = new ConcurrentHashMap<Field,Object>(); // TODO: is there a way to use wildcard instead ?
	private ConcurrentHashMap<Field,Instant> timeUpdateMap = new ConcurrentHashMap<Field,Instant>();
	
	public ConcurrentHashMap<Field, Object> getFieldsMap() {
		return fieldsMap;
	}
	public void setFieldsMap(ConcurrentHashMap<Field, Object> fieldsMap) {
		this.fieldsMap = fieldsMap;
	}
	public ConcurrentHashMap<Field, Instant> getTimeUpdateMap() {
		return timeUpdateMap;
	}
	public void setTimeUpdateMap(ConcurrentHashMap<Field, Instant> timeUpdateMap) {
		this.timeUpdateMap = timeUpdateMap;
	}
	
	public void put(Field field,Object value) {
		this.fieldsMap.put(field,value);
	}
	
	public void putTimestamp(Field field,Instant instant) {
		this.timeUpdateMap.put(field,instant);
	}
	
	public Object get(Field field) throws MarketDataMissingException {
		if(this.getFieldsMap().containsKey(field))
			return this.getFieldsMap().get(field);
		else
			throw new MarketDataMissingException("Market data missing " + field.toString());
	}
	
}
