package spark;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeoutException;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.CoreConfig;
import exceptions.DataServiceStartException;
import marketdata.services.bloomberg.services.BBGReferenceDataService;
import org.apache.spark.sql.execution.streaming.Source;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CoreConfig.class)
public class TestSparkStreaming {


    @Autowired
    private SparkSession sparkSession;
 
 
    @Test
    public void testSparkSession() throws TimeoutException  {
    	writeSourceAsStream(sparkSession);
    }
 
    private static void writeSourceAsStream(SparkSession sparkSession) throws TimeoutException {

        Dataset<Row> dataset = sparkSession.readStream().
                format("utils.CSVMicroBatchReader")
                .load();

        StreamingQuery consoleQuery = dataset
        		.writeStream().format("console").outputMode(OutputMode.Append()).start();

        try {
            consoleQuery.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
