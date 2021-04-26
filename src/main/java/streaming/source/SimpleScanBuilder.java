package streaming.source;

import org.apache.spark.sql.connector.read.Scan;
import org.apache.spark.sql.connector.read.ScanBuilder;

public class SimpleScanBuilder implements ScanBuilder {

	public SimpleScanBuilder() {
		
	}
	
	@Override
	public Scan build() {
		return new SimpleScan();
	}

}
