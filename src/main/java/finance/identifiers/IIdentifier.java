package finance.identifiers;

import finance.instruments.IInstrument;
import finance.instruments.Instrument;
import finance.springBean.Exchange;

public interface IIdentifier {
	public String getName();
	public IInstrument getInstrument();
	public IdentifierType getType();
	public Exchange guessExchange();
}
