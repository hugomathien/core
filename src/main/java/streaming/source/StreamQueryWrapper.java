package streaming.source;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.ListUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.spark.sql.Column;
import event.sequencing.Sequenceable;

public class StreamQueryWrapper implements Sequenceable {
	@Autowired
	private SparkSession sparkSession;
	private MemoryStreamWrapper memoryStream;
	private StreamingQuery query;
	private DataStreamWriter<Row> streamWriter;
	private Thread asyncQueryThread;
	private boolean async = false;
	private List<Function<Dataset<Row>,Dataset<Row>>> transforms;
	private String outputMode = "append" ;
	private String format = "memory";
	private String queryName ="defaultStreamQuery";
	private String checkpointLocation;
	private String path;	
	private String[] partitionColumns;	
	private boolean print = false;

	public StreamQueryWrapper() {
		this.transforms = new ArrayList<Function<Dataset<Row>,Dataset<Row>>>();
	}

	public StreamQueryWrapper(MemoryStreamWrapper memoryStream) {
		this.memoryStream = memoryStream;
		this.transforms = new ArrayList<Function<Dataset<Row>,Dataset<Row>>>();
	}

	public StreamQueryWrapper(MemoryStreamWrapper memoryStream,String outputMode,String format,String queryName,List<Function<Dataset<Row>,Dataset<Row>>> transforms,String... partitionColumns) {
		this.memoryStream = memoryStream;
		this.transforms = transforms;
		this.outputMode = outputMode;
		this.format = format;
		this.queryName = queryName;
		this.partitionColumns = partitionColumns;
	}

	public StreamQueryWrapper(MemoryStreamWrapper memoryStream,String outputMode,String format,String queryName,String checkpointLocation,String path,List<Function<Dataset<Row>,Dataset<Row>>> transforms,String... partitionColumns) {
		this.memoryStream = memoryStream;
		this.transforms = transforms;
		this.outputMode = outputMode;
		this.format = format;
		this.queryName = queryName;
		this.checkpointLocation = checkpointLocation;
		this.path = path;
		this.partitionColumns = partitionColumns;
	}

	@PostConstruct
	public void init() {

		Dataset<Row> df = this.memoryStream.toDF();
		df = combineF(ListUtils.emptyIfNull(transforms)).apply(df);	
		if(this.partitionColumns != null) {
			Column[] colArray = new Column[this.partitionColumns.length];
			for(int i=0;i<this.partitionColumns.length;i++) {
				String columnName = this.partitionColumns[i];
				colArray[i] = df.col(columnName);
			}
			df = df.repartition(colArray);
		}
		
		streamWriter = df
				.writeStream()
				.outputMode(this.outputMode)
				.format(this.format)
				.queryName(this.queryName); // TODO the query gets updated when new data comes in and not manually: is this what we want ?
				//.trigger(Trigger.ProcessingTime("1 second"))
				
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
		asyncQueryThread = new Thread(){
			public void run(){
				try {
					query.awaitTermination();
				} catch (StreamingQueryException e) {
					e.printStackTrace();
				}
			}
		};
		asyncQueryThread.start();
	}


	public MemoryStreamWrapper getMemoryStream() {
		return memoryStream;
	}

	public void setMemoryStream(MemoryStreamWrapper memoryStream) {
		this.memoryStream = memoryStream;
	}

	public StreamingQuery getQuery() {
		return query;
	}

	public void setQuery(StreamingQuery query) {
		this.query = query;
	}

	public DataStreamWriter<Row> getStreamWriter() {
		return streamWriter;
	}

	public void setStreamWriter(DataStreamWriter<Row> streamWriter) {
		this.streamWriter = streamWriter;
	}

	public Thread getAsyncQueryThread() {
		return asyncQueryThread;
	}

	public void setAsyncQueryThread(Thread asyncQueryThread) {
		this.asyncQueryThread = asyncQueryThread;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public SparkSession getSparkSession() {
		return sparkSession;
	}

	public void setSparkSession(SparkSession sparkSession) {
		this.sparkSession = sparkSession;
	}

	public List<Function<Dataset<Row>, Dataset<Row>>> getTransforms() {
		return transforms;
	}

	public void setTransforms(List<Function<Dataset<Row>, Dataset<Row>>> transforms) {
		this.transforms = transforms;
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

	public String getCheckpointLocation() {
		return checkpointLocation;
	}

	public void setCheckpointLocation(String checkpointLocation) {
		this.checkpointLocation = checkpointLocation;
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

	public boolean isPrint() {
		return print;
	}

	public void setPrint(boolean print) {
		this.print = print;
	}

	@Override
	public void execute(Instant t, Object... args) {
		this.query.processAllAvailable();
		if(this.print)
			sparkSession.sql("select * from "+this.queryName).show(false);
	}	

	private static <T> Function<T, T> combineF(List<Function<T, T>> funcs) {
		return funcs.stream().reduce(Function.identity(), Function::andThen);
	}


}
