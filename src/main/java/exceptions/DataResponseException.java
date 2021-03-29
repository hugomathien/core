package exceptions;

public class DataResponseException extends Exception {
	public DataResponseException(String message,Throwable err) {
		super(message,err);
	}
}
