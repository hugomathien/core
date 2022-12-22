package marketdata.services.flatfile;

import event.processing.EventPriorityQueue;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import marketdata.services.base.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import utils.Spark;

import java.time.LocalDate;

@Service("FLAT_FILE_REFERENCE")
@Scope("singleton")
@Lazy(true)
public class FlatFileReferenceDataService extends FlatFileService  implements IReferenceDataService  {
    @Autowired
    private Spark spark;
    @Autowired
    private FlatFileReferenceResponseHandler responseHandler;
    @Autowired
    private EventPriorityQueue queue;

    public FlatFileReferenceDataService() {
        super();
    }

    @Override
    public void init() {
        return;
    }

    @Override
    public boolean isOpened() {
        return true;
    }

    @Override
    public void start() throws DataServiceStartException {
        return;  // TODO: Check Spark service status ?
    }

    @Override
    public AbstractResponseHandler<?> getResponseHandler() {
        return this.responseHandler;
    }


    @Override
    @Value("${flatfile.requestTypes}")
    public void setRequestDiscovery(RequestType[] requestDiscovery) {
        this.requestDiscovery = requestDiscovery;
    }


    @Override
    public void query(DataRequest requestBuilder) throws DataQueryException {
        try {
            switch(requestBuilder.getRequestType()) {
                case UniverseRequest:
                    this.universeRequest(requestBuilder);
                    break;
                default:
                    break;
            }
        } catch (DataQueryException e) {
            e.printStackTrace();
        }
    }


    private Dataset<Row> prepareDataset(DataRequest requestBuilder,String... selectExpr) throws DataQueryException {
        if(!requestBuilder.getParameters().containsKey(RequestParameters.filepath) ||
                !requestBuilder.getParameters().containsKey(RequestParameters.fileformat))
            throw new DataQueryException("Filepath and/or Fileformat parameter are missing from the Flat file.",null);

        String filePath = (String) requestBuilder.getParameters().get(RequestParameters.filepath);
        String fileFormat = (String) requestBuilder.getParameters().get(RequestParameters.fileformat);
        String sqlFilter = (String) requestBuilder.getParameters().getOrDefault((RequestParameters.sql),"1=1");
        boolean header = (boolean) requestBuilder.getParameters().getOrDefault((RequestParameters.header),true);
        String startDate = ((LocalDate) requestBuilder.getParameters().get(RequestParameters.startDate)).toString();
        String endDate = ((LocalDate) requestBuilder.getParameters().get(RequestParameters.endDate)).toString();

        if(selectExpr == null && requestBuilder.getParameters().containsKey(RequestParameters.selectExpr))
            selectExpr = (String[]) requestBuilder.getParameters().get(RequestParameters.selectExpr);
        else if(selectExpr == null && !requestBuilder.getFields().isEmpty())
            selectExpr = requestBuilder.getFields().stream().map(f -> f.name().toLowerCase()).toArray(String[]::new);
        else if (selectExpr == null)
            selectExpr = new String[]{"*"};

        // TODO: add InstrumentType filter
        Dataset<Row> df = spark.sparkSession().read()
                .format(fileFormat)
                .option("inferSchema",true)
                .option("header",header)
                .load(filePath)
                .filter(String.format("date>='%s' and date<='%s'", startDate, endDate))
                .filter(sqlFilter)
                .selectExpr(selectExpr);
        return df;
    }

    private void universeRequest(DataRequest request) throws DataQueryException {
        Dataset<Row> df = this.prepareDataset(request,"datetime as timestamp","instrumentType","universe","ticker").dropDuplicates("ticker");
        df.collectAsList().stream().forEach(this.responseHandler.universeResponse(request));
    }

}
