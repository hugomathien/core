package event.timers;

import config.CoreConfig;
import dataset.AbstractDFWriter;
import event.events.TimerEvent;
import event.processing.CoreEventType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.springframework.core.Ordered;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.concurrent.TimeoutException;

public class TimerDataframeWriterStream extends AbstractDFWriter {
    private DataStreamWriter<Row> streamWriter;
    private String checkpointLocation;
    private boolean async = true;
    private StreamingQuery query;
    private String processingTime = "1 second";
    private boolean processAllAtEachStep = false;

    public TimerDataframeWriterStream() {
        super();
    }

    public TimerDataframeWriterStream(Builder builder) {
        super(builder);
        this.checkpointLocation = builder.checkpointLocation;
        this.async = builder.async;
        this.processingTime = builder.processingTime;
        this.processAllAtEachStep = builder.processAllAtEachStep;
    }

    @Override
    public TimerEvent createEvent(Instant eventTimestamp, Temporal start, Temporal end) {
        TimerEvent te = new TimerEvent(this,eventTimestamp,this, CoreEventType.STREAM_WRITER, Ordered.LOWEST_PRECEDENCE);
        return te;
    }

    @Override
    public void execute(Instant t, Object... args) {
        if(this.query != null) {
            if(this.processAllAtEachStep) // TODO: Om termination cleanup checkpoint dir and metadata
                this.query.processAllAvailable();
            return;
        }

        Dataset<Row> df = this.dfContainer.toDF();
        df = super.repartition(df);

        streamWriter = df
                .writeStream()
                .outputMode(this.outputMode)
                .format(this.format)
                .queryName(this.queryName)
                .option("header",true)
                .trigger(Trigger.ProcessingTime(processingTime));

        if(this.checkpointLocation != null)
            streamWriter = streamWriter.option("checkpointLocation", this.checkpointLocation);
        if(this.path != null)
            streamWriter = streamWriter.option("path", this.path);
        if(this.partitionColumns != null)
            streamWriter = streamWriter.partitionBy(this.partitionColumns);

        try {
            query = streamWriter.start();
           if(async)
                executeAsync();
        } catch (TimeoutException e1) {
            e1.printStackTrace();
        }
    }

    private void executeAsync() {
        new Thread(() -> {
                try {
                    query.awaitTermination();
                } catch (StreamingQueryException e) {
                    e.printStackTrace();
                }
        }).start();
    }

    public DataStreamWriter<Row> getStreamWriter() {
        return streamWriter;
    }

    public void setStreamWriter(DataStreamWriter<Row> streamWriter) {
        this.streamWriter = streamWriter;
    }

    public String getCheckpointLocation() {
        return checkpointLocation;
    }

    public void setCheckpointLocation(String checkpointLocation) {
        this.checkpointLocation = checkpointLocation;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public StreamingQuery getQuery() {
        return query;
    }

    public void setQuery(StreamingQuery query) {
        this.query = query;
    }

    public String getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    public boolean isProcessAllAtEachStep() {
        return processAllAtEachStep;
    }

    public void setProcessAllAtEachStep(boolean processAllAtEachStep) {
        this.processAllAtEachStep = processAllAtEachStep;
    }

    public static class Builder extends AbstractDFWriter.Builder {
        private String checkpointLocation;
        private boolean async = true;
        private String processingTime = "1 second";
        private boolean processAllAtEachStep = false;

        public Builder() {
            super();
        }

        public Builder checkpointLocation(String checkpointLocation) {
            this.checkpointLocation = checkpointLocation;
            return this;
        }

        public Builder async(boolean async) {
            this.async = async;
            return this;
        }

        public Builder processingTime(String processingTime) {
            this.processingTime = processingTime;
            return this;
        }

        public Builder processAllAtEachStep(boolean processAllAtEachStep) {
            this.processAllAtEachStep = processAllAtEachStep;
            return this;
        }


        public TimerDataframeWriterStream build() {
            return CoreConfig.ctx.getBean(TimerDataframeWriterStream.class,this);

        };
    }

}
