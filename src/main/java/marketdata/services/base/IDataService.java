package marketdata.services.base;

import java.util.Arrays;

import exceptions.DataServiceStartException;

public interface IDataService {

	public boolean isOpened();
	public void setOpened(boolean opened);
	public void start() throws DataServiceStartException;
	public RequestType[] getRequestDiscovery();
	public AbstractResponseHandler<?> getResponseHandler();
	default boolean requestIsValid(RequestType requestType) {
		return Arrays.asList(this.getRequestDiscovery()).contains(requestType);
	}
}
