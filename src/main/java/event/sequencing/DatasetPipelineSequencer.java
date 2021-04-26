package event.sequencing;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;

import config.CoreConfig;
import event.events.TimerEvent;
import event.processing.CoreEventType;
import javapipelines.IDatasetTransformer;

public class DatasetPipelineSequencer extends AbstractEventSequencer<DatasetPipelineSequencer> implements Sequenceable {
	@Autowired
	private SparkSession sparkSession;
	private IDatasetTransformer transformer;	
	private String entryTableName;
	private String exitTableName;
	
	
	public DatasetPipelineSequencer() {
		super();
	}

	public DatasetPipelineSequencer(Builder builder) {
		super(builder);
	
		this.entryTableName = builder.entryTableName;	
		this.exitTableName = builder.exitTableName;		
		this.transformer = builder.transformer;
	}
	
	@Override
	public void execute(Instant t, Object... args) {
		Dataset<Row>  df = sparkSession.sql("select * from " + this.entryTableName); // lazy eval	
		// if transform mode
		df = transformer.compute(sparkSession, df, t); // lazy eval
		// if pipeline fit mode
		// 		transformer.fit(df) --> train and save a pipelineModel 
		// if pipeline predict
		// 		transformer.predict(df) --> prediction df can be filtered in the function (if input df is not filtered already)
		df.createOrReplaceTempView(exitTableName);
	}

	@Override
	public TimerEvent createEvent(Instant eventTimestamp, Temporal start, Temporal end) {
		TimerEvent te = new TimerEvent(this,eventTimestamp,this,CoreEventType.DATASET_PIPELINE);
		return te;
	}
	
	@Deprecated
	private static <T> Function<T, T> combineF(List<Function<T, T>> funcs) {
	    return funcs.stream().reduce(Function.identity(), Function::andThen);
	}

	public IDatasetTransformer gettransformer() {
		return transformer;
	}

	public void settransformer(IDatasetTransformer transformer) {
		this.transformer = transformer;
	}

	public String getEntryTableName() {
		return entryTableName;
	}

	public void setEntryTableName(String entryTableName) {
		this.entryTableName = entryTableName;
	}

	public String getExitTableName() {
		return exitTableName;
	}

	public void setExitTableName(String exitTableName) {
		this.exitTableName = exitTableName;
	}



	public static class Builder extends AbstractEventSequencer.Builder<DatasetPipelineSequencer> {
		private String entryTableName;	
		private String exitTableName;	
		private IDatasetTransformer transformer;
		
		public Builder() {
			super();
		}

		@Override
		public DatasetPipelineSequencer build() {
			return CoreConfig.ctx.getBean(DatasetPipelineSequencer.class,this);
		}		

		public Builder entryTableName(String entryTableName) {
			this.entryTableName = entryTableName;
			return this;
		}
		
		public Builder exitTableName(String exitTableName) {
			this.exitTableName = exitTableName;
			return this;
		}
		
		public Builder transformer(IDatasetTransformer transformer) {
			this.transformer = transformer;
			return this;
		}
	}
	
}
