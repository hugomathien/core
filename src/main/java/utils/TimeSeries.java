package utils;

import java.time.temporal.Temporal;
import java.util.concurrent.ConcurrentSkipListMap;

import marketdata.container.AbstractMarketDataContainer;

public class TimeSeries<K extends Temporal,T extends AbstractMarketDataContainer> extends ConcurrentSkipListMap<K,T> {

	public TimeSeries() {
		
	}
	
	public void printTimeSeries() {
		this.entrySet()
		.stream()
		.forEach(e-> System.out.println(e.getKey().toString() +"="+ 
				e.getValue().getFieldsMap().toString()));
	}
}
