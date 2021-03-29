package exceptions;

public class DataQueryException extends Exception {
	public DataQueryException(String message,Throwable err) {
		super(message,err);
	}
}
