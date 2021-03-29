package exceptions;

public class DataPointException extends Exception {
	public DataPointException(String message,Throwable err) {
		super(message,err);
	}
}
