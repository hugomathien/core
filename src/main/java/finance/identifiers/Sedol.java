package finance.identifiers;

import finance.instruments.IInstrument;
import finance.misc.Exchange;
import marketdata.services.bloomberg.utils.TickerSuffix;

public class Sedol extends Identifier implements IBloombergIdentifier {
	
	public Sedol(IInstrument instrument, String name) {
		super(instrument,name,IdentifierType.SEDOL);
	}
	
	public String getBbgQuerySyntax() {
		return null;
	}
	
	public String getTickerWithSuffix() {
		return null;
	}

	@Override
	public TickerSuffix getSuffix() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
