package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.connector.read.InputPartition;
import org.apache.spark.sql.connector.read.PartitionReaderFactory;
import org.apache.spark.sql.connector.read.streaming.MicroBatchStream;
import org.apache.spark.sql.connector.read.streaming.Offset;
import org.apache.spark.sql.execution.streaming.Source;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.apache.spark.sql.sources.StreamSourceProvider;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.unsafe.types.UTF8String;

import com.google.gson.Gson;
import com.opencsv.CSVReader;

import scala.Option;
import scala.Tuple2;
import scala.collection.immutable.Map;

/**
 *
 */
public class CSVMicroBatchReader implements MicroBatchStream,DataSourceRegister {

	private final String filePath = "C:\\Users\\suyux\\Downloads\\sparkStreamingTestFile.csv";
	private int lastCommitted;
	private ReentrantLock reentrantLock;
	private int start;
	private int end;
	private int current;
	private List<Object[]> rows;
	private boolean keepRunning = false;
	private StructType schema;
	private List<Function> converters;
	private final CsvReader csvReader;

	public CSVMicroBatchReader() {

		this.start = 0;
		this.end = 0;
		this.current = 0;
		this.rows = new ArrayList<>();
		this.reentrantLock = new ReentrantLock();
		createSchema();
		populateConverters();
		this.csvReader = new CsvReader(converters);
		new Thread(csvReader).start();
	}

	@Override
	public Offset initialOffset() {
		return new CSVOffset(start);
	}

	@Override
	public Offset deserializeOffset(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, CSVOffset.class);
	}

	@Override
	public void commit(Offset end) {
		int offset = ((CSVOffset) end).getOffset();
		int actualOffset = offset - lastCommitted;
		try {
			reentrantLock.lock();
			rows = new ArrayList<>(rows.subList(actualOffset, rows.size()));
		} finally {
			reentrantLock.unlock();
		}

		this.lastCommitted = offset;
	}

	@Override
	public Offset latestOffset() {
		return new CSVOffset(this.lastCommitted);
	}

	@Override
	public InputPartition[] planInputPartitions(Offset start, Offset end) {
		List<Object[]> rowChunk;
		try {
			int actualStart = ((CSVOffset) start).getOffset() - lastCommitted;
			int actualEnd = ((CSVOffset) end).getOffset() - lastCommitted;
			reentrantLock.lock();
			rowChunk = new ArrayList<>(rows.subList(actualStart, actualEnd));
		} finally {
			reentrantLock.unlock();
		}
		InputPartition[] partarr = {new CSVMicroBatchPartition(rowChunk)};
		return partarr;
	}

	@Override
	public PartitionReaderFactory createReaderFactory() {
		return new CSVMicroBatchPartitionReaderFactory();
	}

	private void createSchema() {
		StructField[] structFields = new StructField[]{
				new StructField("Name", DataTypes.StringType, true, Metadata.empty()),
				new StructField("Price", DataTypes.DoubleType, true, Metadata.empty()),
				new StructField("Volume", DataTypes.IntegerType, true, Metadata.empty())	     
		};
		this.schema = new StructType(structFields);
	}

	private void populateConverters() {
		StructField[] fields = this.schema.fields();
		converters = new ArrayList<>(fields.length);
		for (StructField structField : fields) {

			if (structField.dataType() == DataTypes.StringType)
				converters.add(stringConverter);
			else if (structField.dataType() == DataTypes.IntegerType)
				converters.add(intConverter);
			else if (structField.dataType() == DataTypes.DoubleType)
				converters.add(doubleConverter);
		}
	}

	@Override
	public void stop() {
		keepRunning = false;
	}

	/**
	 *
	 */
	class CsvReader implements Runnable, Serializable {
		private FileReader filereader;
		private Iterator<String[]> iterator;
		private List<Function> converters;

		CsvReader(List<Function> converters) {

			try {
				this.converters = converters;
				this.filereader = new FileReader(filePath);
				this.iterator = new CSVReader(filereader).iterator();
				iterator.next();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			keepRunning = true;
			while (iterator.hasNext() && keepRunning) {
				try {
					String[] row = iterator.next();
					Object[] convertedValues = new Object[row.length];
					for (int i = 0; i < row.length; i++) {
						convertedValues[i] = converters.get(i).apply(row[i]);
					}
					reentrantLock.lock();
					rows.add(convertedValues);
					current++;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					reentrantLock.unlock();
				}
			}
		}

		private void close() {
			try {
				filereader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Function<String, UTF8String> stringConverter = (val) -> UTF8String.fromString(val);

	private Function<String, Integer> intConverter = (val) -> Integer.parseInt(val);

	private Function<String, Double> doubleConverter = (val) -> Double.parseDouble(val);

	@Override
	public String shortName() {
		// TODO Auto-generated method stub
		return "mycustomcsvsource";
	}


}