package event.sequencing;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import config.CoreConfig;
import event.events.TimerEvent;
import event.processing.CoreEventType;
import streaming.source.MemoryStreamWrapper;
import streaming.source.StreamQueryWrapper;

/**
 * Runs a query on a dataframe created from a memory stream
 * The dataframe can be transformed by one or more strategies (i.e: uses lambda composition)
 * The query can either run on a timer or continuously on its own thread
 * The query is given a name so it can be accessed elsewhere using Spark SQL
 * 
 * @author hugo
 *
 */
public class StreamQuerySequencer extends AbstractEventSequencer<StreamQueryWrapper> {
	private MemoryStreamWrapper memoryStream;
	private StreamQueryWrapper query;	
	private String outputMode = "append" ;
	private String format = "memory";
	private String queryName ="defaultStreamQuery";
	private String checkpointLocation;
	private String path;
	private String[] partitionColumns;	
	private List<Function<Dataset<Row>,Dataset<Row>>> transforms; // TODO: Improve this (support for each transforms + pass timestamp ?)

	public StreamQuerySequencer() {
		super();
	}

	public StreamQuerySequencer(Builder builder) {
		super(builder);
		if(builder.memoryStream != null)
			this.memoryStream = builder.memoryStream;
		else 
			this.memoryStream = CoreConfig.ctx.getBean(MemoryStreamWrapper.class); // TODO: Creates a new memory stream is this what we want ?

		this.outputMode = builder.outputMode;
		this.format = builder.format;
		this.queryName = builder.queryName;
		this.checkpointLocation = builder.checkpointLocation;
		this.path = builder.path;	
		this.partitionColumns = builder.partitionColumns;
		this.transforms = builder.transforms;
	}

	@PostConstruct
	public void init() {
		if(this.query == null) {
			this.query = CoreConfig.ctx.getBean(StreamQueryWrapper.class,
					this.memoryStream,
					this.outputMode,
					this.format,
					this.queryName,
					this.checkpointLocation,
					this.path,
					this.transforms,
					this.partitionColumns);
		}
		super.init();
	}

	@Override
	public TimerEvent createEvent(Instant eventTimestamp, Temporal start, Temporal end) {
		TimerEvent te = new TimerEvent(this,eventTimestamp,query,CoreEventType.STREAMING_QUERY);
		return te;
	}

	public static class Builder extends AbstractEventSequencer.Builder<StreamQueryWrapper> {
		private MemoryStreamWrapper memoryStream;
		private String outputMode = "append" ;
		private String format = "memory";
		private String queryName ="defaultStreamQuery";
		private String checkpointLocation;
		private String path;	
		private String[] partitionColumns;
		private List<Function<Dataset<Row>,Dataset<Row>>> transforms;
		
		public Builder() {
			super();
			this.transforms = new ArrayList<Function<Dataset<Row>,Dataset<Row>>>();
		}

		@Override
		public StreamQuerySequencer build() {
			return CoreConfig.ctx.getBean(StreamQuerySequencer.class,this);
		}		

		public Builder outputMode(String outputMode) {
			this.outputMode = outputMode;
			return this;
		}

		public Builder format(String format) {
			this.format = format;
			return this;
		}

		public Builder queryName(String queryName) {
			this.queryName = queryName;
			return this;
		}

		public Builder checkpointLocation(String checkpointLocation) {
			this.checkpointLocation = checkpointLocation;
			return this;
		}

		public Builder memoryStream(MemoryStreamWrapper memoryStream) {
			this.memoryStream = memoryStream;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}
		
		public Builder partitionColumns(String... partitionColumns) {
			this.partitionColumns = partitionColumns;
			return this;
		}
		
		public Builder transforms(List<Function<Dataset<Row>,Dataset<Row>>> transforms) {
			this.transforms = transforms;
			return this;
		}
	}


	public StreamQueryWrapper getQuery() {
		return query;
	}

	public void setQuery(StreamQueryWrapper query) {
		this.query = query;
	}

	public MemoryStreamWrapper getMemoryStream() {
		return memoryStream;
	}

	public void setMemoryStream(MemoryStreamWrapper memoryStream) {
		this.memoryStream = memoryStream;
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

	public List<Function<Dataset<Row>, Dataset<Row>>> getTransforms() {
		return transforms;
	}

	public void setTransforms(List<Function<Dataset<Row>, Dataset<Row>>> transforms) {
		this.transforms = transforms;
	}
	
}
