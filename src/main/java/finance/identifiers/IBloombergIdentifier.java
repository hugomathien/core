package finance.identifiers;

import marketdata.services.bloomberg.utils.TickerSuffix;

public interface IBloombergIdentifier {
	public String getBbgQuerySyntax();
	public TickerSuffix getSuffix();
}
