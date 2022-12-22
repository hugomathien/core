package marketdata.services.flatfile;

import marketdata.services.base.AbstractDataService;
import marketdata.services.base.DataServiceEnum;

public abstract class FlatFileService extends AbstractDataService {

    public FlatFileService() {
        super(DataServiceEnum.FLAT_FILE);
    }

}
