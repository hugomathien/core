package utils;

import java.time.temporal.Temporal;
import java.util.concurrent.ConcurrentSkipListMap;

public class TimeSeries<K extends Temporal,T> extends ConcurrentSkipListMap<K,T> {

	public TimeSeries() {
		
	}
}
