package exceptions;

public class MarketDataMissingException extends NullPointerException {

	public MarketDataMissingException(String message) {
		super(message);
	}
}
