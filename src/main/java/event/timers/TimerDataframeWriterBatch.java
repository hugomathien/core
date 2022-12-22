package event.timers;

import config.CoreConfig;
import dataset.AbstractDFWriter;
import event.events.TimerEvent;
import event.processing.CoreEventType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.core.Ordered;

import java.time.Instant;
import java.time.temporal.Temporal;

public class TimerDataframeWriterBatch extends AbstractDFWriter {

    public TimerDataframeWriterBatch() {
        super();
    }
    public TimerDataframeWriterBatch(Builder builder) {
        super(builder);
    }

    @Override
    public TimerEvent createEvent(Instant eventTimestamp, Temporal start, Temporal end) {
        TimerEvent te = new TimerEvent(this,eventTimestamp,this, CoreEventType.BATCH_WRITER, Ordered.LOWEST_PRECEDENCE);
        return te;
    }

    @Override
    public void execute(Instant t, Object... args) {
        Dataset<Row> df = this.dfContainer.toDF();
        df = this.repartition(df);
        if(this.format == "console")
            df.show();
        else {
            if(this.partitionColumns != null)
                df.coalesce(1).write().partitionBy(this.partitionColumns).format(this.format).mode(this.outputMode).option("header", true).save(this.path);
            else
                df.coalesce(1).write().format(this.format).mode(this.outputMode).option("header", true).save(this.path);
        }

        df.createOrReplaceTempView(this.queryName);
        this.dfContainer.reset();
    }

    public static class Builder extends AbstractDFWriter.Builder {

        public Builder() {
            super();
        }

        public TimerDataframeWriterBatch build() {
            return CoreConfig.ctx.getBean(TimerDataframeWriterBatch.class,this);

        };
    }


}
