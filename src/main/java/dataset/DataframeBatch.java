package dataset;

import config.CoreConfig;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;

import java.util.ArrayList;

public class DataframeBatch extends AbstractDFContainer {
    private ArrayList<Row> rowList;


    public DataframeBatch(StructField[] structFields) {
        super(structFields);
        this.rowList = new ArrayList<Row>();
    }

    @Override
    public Dataset<Row> toDF() {
        Dataset<Row> dataset = CoreConfig.services().sparkSession().createDataFrame(this.rowList, this.schema);
        return dataset;
    }

    @Override
    public void addData(ArrayList<Row> rowList) {
        this.rowList.addAll(rowList);
    }

    @Override
    public void reset() {
        this.rowList.clear();
    }
}
