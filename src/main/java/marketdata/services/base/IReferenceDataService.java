package marketdata.services.base;

import exceptions.DataQueryException;

public interface IReferenceDataService<K> extends IDataService {
	public K query(DataRequest<K> dataRequest,RequestType requestType) throws DataQueryException;
	public void setRequestDiscovery(RequestType[] requestDiscovery);

}
