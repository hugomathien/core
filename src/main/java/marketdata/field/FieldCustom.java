package marketdata.field;

import java.util.HashMap;

import finance.misc.TradingPhaseEnum;
import marketdata.services.base.DataServiceEnum;

public class FieldCustom implements Field {

	public static HashMap<String,FieldCustom> customFieldMap = new HashMap<String,FieldCustom>();
	private String name;
	private Class<?> type;
	private boolean isPriceData = true;
	private HashMap<DataServiceEnum,String> mapDataServiceToString = new HashMap<DataServiceEnum,String>();
	private TradingPhaseEnum tradingPhaseEnum;
	private boolean tradingPhaseStart = true;
	
	
	protected FieldCustom(String name,Class<?> type) {
		this.name = name;
		this.type = type;
		customFieldMap.put(name, this);
	}
	
	protected FieldCustom(String name,String bloomberg,Class<?> type) {
		this.name = name;
		this.type = type;
		customFieldMap.put(name, this);
	}

	public String name() {
		return name;
	}

	public Class<?> type() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public boolean isPriceData() {
		return isPriceData;
	}

	public void setIsPriceData(boolean isPriceData) {
		this.isPriceData = isPriceData;
	}

	public HashMap<DataServiceEnum, String> getMapDataServiceToString() {
		return mapDataServiceToString;
	}
	
	public String toString() {
		return this.name;
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
