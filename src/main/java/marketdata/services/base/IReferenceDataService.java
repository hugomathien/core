package marketdata.services.base;

import exceptions.DataQueryException;

public interface IReferenceDataService extends IDataService {
	public void setRequestDiscovery(RequestType[] requestDiscovery);

}
