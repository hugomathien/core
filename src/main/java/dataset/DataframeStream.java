package dataset;

import config.CoreConfig;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.execution.streaming.MemoryStream;
import org.apache.spark.sql.types.StructField;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;

public class DataframeStream extends AbstractDFContainer {
    private MemoryStream<Row> sparkStream;
    private ExpressionEncoder<Row> encoder;

    public DataframeStream(StructField[] structFields) {
        super(structFields);
        encoder = RowEncoder.apply(schema);
        this.sparkStream = new MemoryStream<Row>(1, CoreConfig.services().sparkSession().sqlContext(), null, encoder);
    }

    @Override
    public Dataset<Row> toDF() {
        return this.sparkStream.toDF().as(encoder);
    }

    @Override
    public void addData(ArrayList<Row> rowList) {
        Seq<Row> seq = JavaConverters.asScalaIteratorConverter(rowList.iterator()).asScala().toSeq();
        // TODO: Should throw an error if schema and data don't match
        this.sparkStream.addData(seq);
    }

    @Override
    public void reset() {
        this.sparkStream.reset();
    }


}
