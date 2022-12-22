package dataset;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;

public abstract class AbstractDFContainer {
    protected StructField[] structFields;
    protected StructType schema;

    public AbstractDFContainer(StructField[] structFields) {
        this.structFields = structFields;
        this.schema = new StructType(structFields);
    }

    public abstract Dataset<Row> toDF();

    public abstract void addData(ArrayList<Row> rowList);

    public static AbstractDFContainer makeContainer(StateToDataframe.DFContainerType dfContainerType, StructField[] structFields) {
        switch (dfContainerType) {
            case MEMORY_STREAM:
                return new DataframeStream(structFields);
            case BATCH:
                return new DataframeBatch(structFields);
        }
        return null;
    }

    public StructField[] getStructFields() {
        return structFields;
    }

    public void setStructFields(StructField[] structFields) {
        this.structFields = structFields;
    }

    public abstract void reset();
}
