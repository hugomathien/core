package exceptions;

public class DataServiceStartException extends Exception {
	
	public DataServiceStartException(String message,Throwable err) {
		super(message,err);
	}
}
