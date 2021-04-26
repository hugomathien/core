package finance.identifiers;

import finance.instruments.IInstrument;
import finance.instruments.Instrument;
import finance.misc.Exchange;

@Deprecated
public interface IIdentifier {
	public String getName();
	public IInstrument getInstrument();
	public IdentifierType getType();
	public Exchange guessExchange();
}
