package marketdata.field;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;

import com.bloomberglp.blpapi.Element;

import config.CoreConfig;
import finance.springBean.Exchange;
import finance.springBean.TradingSession;
import finance.tradingcycle.TradingPhaseEnum;
import marketdata.services.base.DataServiceEnum;
import utils.PriceCheck;

public interface Field {
	public String name();
	public Class<?> type();
	public void setType(Class<?> type);
	public HashMap<DataServiceEnum,String> getMapDataServiceToString();
	public boolean isPriceData();
	public void setIsPriceData(boolean isPriceData);
	public TradingPhaseEnum tradingPhase();
	public void setTradingPhaseEnum(TradingPhaseEnum tradingPhaseEnum);
	public boolean tradingPhaseStart();
	public void setTradingPhaseStart(boolean tradingPhaseStart);
	
	public static Field get(String name) {
		Field field;
		try {
			field = FieldEnum.valueOf(name);
			return field;
		}
		catch(IllegalArgumentException e) {
			if(FieldCustom.customFieldMap.containsKey(name)) {
				field = FieldCustom.customFieldMap.get(name);
			}
			else {
				field = new FieldCustom(name,String.class); // TODO default to string type is it the best ?
			}
			return field;
		}
	}
	
	public default Object getValueFromBloombergElement(Element value) {
		if(type() == Double.class) {
			Double val = value.getValueAsFloat64();
			return type().cast(val);
		}
		else if(type() == Integer.class) {
			Integer val = value.getValueAsInt32();
			return type().cast(val);
		}
		else if(type() == Long.class) {
			Long val = value.getValueAsInt64();
			return type().cast(val);
		}
		else if(type() == String.class) {
			String val = value.getValueAsString();
			return type().cast(val);
		}
		
		return value.toString();
	}
	
	public default boolean valueIsValid(Object v) {
		return this.isPriceData() ? PriceCheck.validPrice((Double) v) : true;
	}
	
	public default Object cast(Object val) {
		if(val instanceof char[])
			return String.valueOf((char[]) val);
		try {
			if(this.type().equals(Long.class))
				return (val instanceof Double) ? ((Double) val).longValue() : Long.valueOf(val.toString());
			else if(this.type().equals(Integer.class))
				return (val instanceof Double) ? ((Double) val).longValue() : Integer.valueOf(val.toString());
			else if(this.type().equals(Double.class))
				return val;
			else if(this.type().equals(String.class))
				return val.toString();
			else if(this.type().equals(Float.class))
				return Double.valueOf((Float) val).doubleValue();
			else if(this.type().equals(Integer.class))
				return this.type().cast(val);
		}
		catch(NumberFormatException e) {
			return this.type().cast(val);
		}
		catch(ClassCastException | NullPointerException e) {
			return val;
		}
		return val;
	}
	
	public default ZonedDateTime assignTimeStamp(LocalDate ld,Exchange exchange) {
		if(this.tradingPhase() == null)
			return ld.atStartOfDay(CoreConfig.GLOBAL_ZONE_ID);
		TradingSession tradingSession = exchange.getTradingSession(this.tradingPhase()); 
		if(this.tradingPhaseStart())
			return tradingSession.getStart(ld, exchange.getDateTimeZone());				
		else
			return tradingSession.getEnd(ld, exchange.getDateTimeZone());
	}
	
	public default String name(DataServiceEnum service) {
		if(this.getMapDataServiceToString().containsKey(service))
			return this.getMapDataServiceToString().get(service);
		else
			return name();
	}
	
	
	
}
