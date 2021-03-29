package finance.identifiers;

import java.util.HashMap;
import java.util.Map;

import config.CoreConfig;
import finance.instruments.ETF;
import finance.instruments.FX;
import finance.instruments.Future;
import finance.instruments.IInstrument;
import finance.instruments.Index;
import finance.instruments.SingleStock;
import finance.springBean.Exchange;
import marketdata.services.bloomberg.enumeration.TickerSuffix;

public class Ticker extends Identifier implements IBloombergIdentifier{
	private static Map<String,String> tickerMicMap = new HashMap<String,String>();
	
	// TODO: Externalize in configuration files
	static {
		tickerMicMap.put("FP", "XPAR");
		tickerMicMap.put("SM", "XMAD");
		tickerMicMap.put("SQ", "XMAD");
		tickerMicMap.put("LN", "XLON");
		tickerMicMap.put("GY", "XETR");
		tickerMicMap.put("GR", "XETR");
		tickerMicMap.put("NA", "XAMS");
		tickerMicMap.put("VX", "XSWX");
		tickerMicMap.put("SW", "XSWX");
		tickerMicMap.put("IM", "XMIL");
		tickerMicMap.put("BB", "XBRU");
		tickerMicMap.put("DC", "XCSE");
		tickerMicMap.put("FH", "XHEL");
		tickerMicMap.put("LS", "XLUS");
		tickerMicMap.put("NO", "XOSL");
		tickerMicMap.put("SS", "XSTO");
		tickerMicMap.put("AV", "XWBO");
		tickerMicMap.put("ID", "XDUB");
	}
	
	public Ticker(IInstrument instrument,String name) {
		super(instrument,name,IdentifierType.TICKER);
	}
	
	@Override
	public TickerSuffix getSuffix() {
		TickerSuffix suffix;
		if(this.getInstrument() instanceof SingleStock)
			return TickerSuffix.Equity;
		else if(this.getInstrument() instanceof Index)
			return TickerSuffix.Index;
		else if(this.getInstrument() instanceof ETF)
			return TickerSuffix.Equity;
		else if(this.getInstrument() instanceof FX)
			return TickerSuffix.Curncy;
		else if(this.getInstrument() instanceof Future)
			return TickerSuffix.Index;
		else
			return null;
	}
	
	public String getTickerWithSuffix() {
		String name = this.getName();
		TickerSuffix suffix = this.getSuffix();
		String tickerWithSuffix = name + " " + suffix.toString();
		return tickerWithSuffix;
	}
	
	public String getBbgQuerySyntax() {
		String tickerWithSuffix = this.getTickerWithSuffix();
		return "/ticker/" + tickerWithSuffix;
	}
	
	@Override
	public Exchange guessExchange() {
		String substring = this.getName().substring(Math.max(this.getName().length() - 2, 0));
		
		if(!tickerMicMap.containsKey(substring))
			return null;
		
		String mic = tickerMicMap.get(substring);
		return CoreConfig.services().getExchange(mic);
	}
	
	public static String getPrimaryTicker(String ticker) {
		String subEnd = ticker.substring(Math.max(ticker.length() - 3, 0));
		int idx = ticker.length() - 3;
		String subStart = ticker.substring(0, idx);
		
		switch(subEnd) {
		case " GR":
			ticker = subStart + " GY";
		}
		
		return ticker;
	}
}
