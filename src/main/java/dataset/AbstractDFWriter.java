package dataset;

import event.timers.AbstractEventSequencer;
import event.timers.Sequenceable;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public abstract class AbstractDFWriter extends AbstractEventSequencer implements Sequenceable {
    protected AbstractDFContainer dfContainer;
    protected String outputMode = "append" ;
    protected String format = "memory";
    protected String queryName ="defaultQuery";
    protected String path;
    protected String[] partitionColumns;

    public AbstractDFWriter() {

    }

    public AbstractDFWriter(Builder builder) {
        super(builder);
        this.outputMode = builder.outputMode;
        this.format = builder.format;
        this.queryName = builder.queryName;
        this.path = builder.path;
        this.partitionColumns = builder.partitionColumns;
        this.dfContainer = builder.dfContainer;
        this.path = builder.path;
    }

    public String getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(String outputMode) {
        this.outputMode = outputMode;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getPartitionColumns() {
        return partitionColumns;
    }

    public void setPartitionColumns(String[] partitionColumns) {
        this.partitionColumns = partitionColumns;
    }

    public AbstractDFContainer getDfContainer() {
        return dfContainer;
    }

    public void setDfContainer(AbstractDFContainer dfContainer) {
        this.dfContainer = dfContainer;
    }

    protected Dataset<Row> repartition(Dataset<Row> df) {
        if(this.partitionColumns != null) {
            Column[] colArray = new Column[this.partitionColumns.length];
            for(int i=0;i<this.partitionColumns.length;i++) {
                String columnName = this.partitionColumns[i];
                colArray[i] = df.col(columnName);
            }
            df = df.repartition(colArray);
        }
        return df;
    }

    public static abstract class Builder extends AbstractEventSequencer.Builder {
        private String outputMode = "append" ;
        private String format = "memory";
        private String queryName ="defaultQuery";
        private String path;
        private String[] partitionColumns;
        private AbstractDFContainer dfContainer;

        public Builder() {

        }

        public Builder outputMode(String outputMode) {
            this.outputMode = outputMode;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder queryName(String queryName) {
            this.queryName = queryName;
            return this;
        }

        public Builder partitionColumns(String[] partitionColumns) {
            this.partitionColumns = partitionColumns;
            return this;
        }

        public Builder dfContainer(AbstractDFContainer dfContainer) {
            this.dfContainer = dfContainer;
            return this;
        }

        public abstract AbstractDFWriter build();
    }

}
