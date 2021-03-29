package marketdata.services.base;

import exceptions.DataQueryException;

public interface IRealTimeDataService extends IDataService {
	public void query(DataRequest<?> dataRequest) throws DataQueryException;
}
