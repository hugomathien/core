package marketdata.field;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.HashMap;
import marketdata.services.bloomberg.BBGService;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;

import config.CoreConfig;
import finance.misc.Exchange;
import finance.misc.TradingPhaseEnum;
import finance.misc.TradingSession;
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
				field = new FieldCustom(name,Double.class); // TODO default to double type is it the best ?
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
		else if(type() == LocalDate.class) {
			// TODO: dates seem to be returned as long but is it always the case(i.e: there is a getValueAsDate() method ) ?
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			long dateInt = value.getValueAsInt64();
			String dateStr = String.valueOf(dateInt);
			LocalDate val = LocalDate.parse(dateStr,formatter);
			return val;
		}
		else if(type() == java.sql.Date.class) {
			// TODO: dates seem to be returned as long but is it always the case(i.e: there is a getValueAsDate() method ) ?
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			long dateInt = value.getValueAsInt64();
			String dateStr = String.valueOf(dateInt);
			LocalDate val = LocalDate.parse(dateStr,formatter);
			return java.sql.Date.valueOf(val);
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
	
	public default ZonedDateTime assignTimeStamp(LocalDate ld,Exchange exchange) throws Exception {
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
	
	public default DataType sparkDataType() {
		if(type() == Double.class) {
			return DataTypes.DoubleType;
		}
		else if(type() == Integer.class) {
			return DataTypes.IntegerType;
		}
		else if(type() == Long.class) {
			return DataTypes.LongType;
		}
		else if(type() == String.class) {
			return DataTypes.StringType;
		}
		else if(type() == Float.class) {
			return DataTypes.FloatType;
		}
		else if(type() == LocalDate.class) {
			return DataTypes.DateType;
		}
		else if(type() == Instant.class) {
			return DataTypes.TimestampType;
		}
		else if(type() == java.sql.Date.class) {
			return DataTypes.DateType;
		}
		else {
			return DataTypes.StringType;
		}
		
	}
	
	
}
