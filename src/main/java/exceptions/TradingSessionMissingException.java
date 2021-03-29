package exceptions;

public class TradingSessionMissingException extends NullPointerException {
	public TradingSessionMissingException(String message) {
		super(message);
	}
}
