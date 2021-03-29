package marketdata.series;

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import marketdata.container.AbstractMarketDataContainer;
import utils.TimeSeries;

public abstract class MarketDataSeries<T extends Temporal,C extends AbstractMarketDataContainer> implements IMarketDataSeries<T,C> {
	public MarketDataSeries() {
		
	}
	
	public abstract TimeSeries<T,C> getTimeSeries(Integer interval);
	public abstract TimeSeries<T,C> getTimeSeries();
	
	public void printTimeSeries(Temporal start, Temporal end) {
		this.getTimeSeries()
		.entrySet()
		.stream()
		.filter(x -> start.until(x.getKey(), ChronoUnit.MILLIS) >= 0)
		.filter(x -> x.getKey().until(end, ChronoUnit.MILLIS) >= 0)
		.map(x -> x.getKey().toString() + " " + x.getValue().getFieldsMap().toString())
		.forEach(x -> System.out.println(x));
	}
	
	public void printTimeSeries() {
		this.getTimeSeries()
		.entrySet()
		.stream()
		.map(x -> x.getKey().toString() + " " + x.getValue().getFieldsMap().toString())
		.forEach(x -> System.out.println(x));
	}
	
	
	
}
