package finance.identifiers;

import java.util.HashMap;
import java.util.Map;

import config.CoreConfig;
import finance.instruments.IInstrument;
import finance.misc.Exchange;

public class Ric extends Identifier {
	private static Map<String,String> ricMicMap = new HashMap<String,String>();
	
	// TODO: Externalize in configuration files
	static {
		ricMicMap.put("PA", "XPAR");
		ricMicMap.put("MC", "XMAD");
		ricMicMap.put(".L", "XLON");
		ricMicMap.put("DE", "XETR");
		ricMicMap.put("AS", "XAMS");
		ricMicMap.put("VX", "XSWX");
		ricMicMap.put("SW", "XSWX");
		ricMicMap.put("MI", "XMIL");
		ricMicMap.put("BR", "XBRU");
		ricMicMap.put("CO", "XCSE");
		ricMicMap.put("HE", "XHEL");
		ricMicMap.put("LS", "XLUS");
		ricMicMap.put("OL", "XOSL");
		ricMicMap.put("ST", "XSTO");
		ricMicMap.put(".S", "XSWX");
		ricMicMap.put("VI", "XWBO");
		ricMicMap.put(".I", "XDUB");
	}
	
	public Ric(IInstrument instrument,String name) {
		super(instrument,name,IdentifierType.RIC);
	}
	
	@Override
	public Exchange guessExchange() {
		String substring = this.getName().substring(Math.max(this.getName().length() - 2, 0));
		if(!ricMicMap.containsKey(substring))
			return null;
		
		String mic = ricMicMap.get(substring);
		return CoreConfig.services().getExchange(mic);
	}
}
