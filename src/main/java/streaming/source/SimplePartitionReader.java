package streaming.source;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.read.PartitionReader;
import org.apache.spark.unsafe.types.UTF8String;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Service;

import scala.collection.JavaConversions;


@Service
@Scope("singleton")
@Lazy(true)
public class SimplePartitionReader implements PartitionReader<InternalRow> {
	String[] vals;
	int index;
	
	public SimplePartitionReader() {
		this.vals = new String[]{"test number 1", "test number 2", "test number 3", "test number 4", "test number 5"};
		this.index = 0;
	}
	
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean next() throws IOException {
		return this.index < vals.length;
	}

	@Override
	public InternalRow get() {
		String stringValue = vals[index];
		UTF8String stringUtf = stringConverter.apply(stringValue);
		//InternalRow row = (InternalRow) RowFactory.create(stringUtf);
		Object[] objrow = new Object[] {stringUtf};
		index += 1;
        return InternalRow.apply(JavaConversions.asScalaBuffer(Arrays.asList(objrow)).seq());
        
	}

	private Function<String, UTF8String> stringConverter = (val) -> UTF8String.fromString(val);

}