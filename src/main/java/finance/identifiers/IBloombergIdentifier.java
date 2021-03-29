package finance.identifiers;

import marketdata.services.bloomberg.enumeration.TickerSuffix;

public interface IBloombergIdentifier {
	public String getBbgQuerySyntax();
	public TickerSuffix getSuffix();
}
