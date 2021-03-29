package marketdata.field;

import java.beans.Transient;
import java.io.Serializable;
import java.util.HashMap;

import finance.tradingcycle.TradingPhaseEnum;
import marketdata.services.base.DataServiceEnum;

public enum FieldEnum implements Field, Serializable {
	LAST_PRICE(Double.class,true),
	PX_LAST(Double.class,true);

	private Class<?> type;
	private boolean isPriceData = true;
	private HashMap<DataServiceEnum,String> mapDataServiceToString = new HashMap<DataServiceEnum,String>();
	private TradingPhaseEnum tradingPhaseEnum;
	private boolean tradingPhaseStart = true;
	
	private FieldEnum(Class<?> type) {
		this.type = type;
	}
	
	private FieldEnum(Class<?> type, boolean priceData) {
		this.type = type;
		this.isPriceData = priceData;
	}
	
	@Transient
	public Class<?> type() {
		return this.type;
	}
	
	public boolean isPriceData() {
		return this.isPriceData;
	}
	
	public HashMap<DataServiceEnum, String> getMapDataServiceToString() {
		return this.mapDataServiceToString;
	}
	
	public void setType(Class<?> type) {
		this.type = type;
	}
	
	public void setIsPriceData(boolean isPriceData) {
		this.isPriceData = isPriceData;
	}
	
	public String toString() {
		return this.name();
	}

	@Override
	public TradingPhaseEnum tradingPhase() {
		return tradingPhaseEnum;
	}

	@Override
	public boolean tradingPhaseStart() {
		return tradingPhaseStart;
	}

	@Override
	public void setTradingPhaseEnum(TradingPhaseEnum tradingPhaseEnum) {
		this.tradingPhaseEnum = tradingPhaseEnum;
	}

	@Override
	public void setTradingPhaseStart(boolean tradingPhaseStart) {
		this.tradingPhaseStart = tradingPhaseStart;		
	}
}
