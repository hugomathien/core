package finance.identifiers;

import java.util.HashMap;
import java.util.Map;

import config.CoreConfig;
import finance.instruments.IInstrument;
import finance.misc.Exchange;

public class Ric extends Identifier {
	public static Map<String,String> ricExchangeCodeMap = new HashMap<String,String>();
	
	// TODO: Externalize in configuration files
	static {
		ricExchangeCodeMap.put("PA", "FP");
		ricExchangeCodeMap.put("MC", "SM");
		ricExchangeCodeMap.put(".L", "LN");
		ricExchangeCodeMap.put("DE", "GY");
		ricExchangeCodeMap.put("AS", "NA");
		ricExchangeCodeMap.put("VX", "VX");
		ricExchangeCodeMap.put("SW", "SW");
		ricExchangeCodeMap.put("MI", "IN");
		ricExchangeCodeMap.put("BR", "BB");
		ricExchangeCodeMap.put("CO", "DC");
		ricExchangeCodeMap.put("HE", "FH");
		ricExchangeCodeMap.put("LS", "LS");
		ricExchangeCodeMap.put("OL", "NO");
		ricExchangeCodeMap.put("ST", "SS");
		ricExchangeCodeMap.put(".S", "SW");
		ricExchangeCodeMap.put("VI", "AV");
		ricExchangeCodeMap.put(".I", "ID");
	}
	
	public Ric(IInstrument instrument,String name) {
		super(instrument,name,IdentifierType.RIC);
	}
	

}
