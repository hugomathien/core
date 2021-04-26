package streaming.source;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.execution.streaming.MemoryStream;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;

public class MemoryStreamWrapper {
	@Autowired
	private SparkSession sparkSession;
	private MemoryStream<Row> sparkStream; 
	private StructField[] structFields;
	private StructType schema;
	private ExpressionEncoder<Row> encoder;
	
	public MemoryStreamWrapper() {
		
	}
	
	public MemoryStreamWrapper(StructField[] structFields) {
		init(structFields);
	}
	
	public void init(StructField[] structFields) {
		this.structFields = structFields;
		
		schema = new StructType(structFields);
		encoder = RowEncoder.apply(schema);
		this.sparkStream = new MemoryStream<Row>(1, sparkSession.sqlContext(), null, encoder);
	}

	public SparkSession getSparkSession() {
		return sparkSession;
	}

	public void setSparkSession(SparkSession sparkSession) {
		this.sparkSession = sparkSession;
	}

	public MemoryStream<Row> getSparkStream() {
		return sparkStream;
	}

	public void setSparkStream(MemoryStream<Row> sparkStream) {
		this.sparkStream = sparkStream;
	}

	public StructField[] getStructFields() {
		return structFields;
	}

	public void setStructFields(StructField[] structFields) {
		this.structFields = structFields;
	}

	public StructType getSchema() {
		return schema;
	}

	public void setSchema(StructType schema) {
		this.schema = schema;
	}

	public ExpressionEncoder<Row> getEncoder() {
		return encoder;
	}

	public void setEncoder(ExpressionEncoder<Row> encoder) {
		this.encoder = encoder;
	}
	
	public Dataset<Row> toDF() {
		return this.sparkStream.toDF().as(encoder);
	}
	
}
