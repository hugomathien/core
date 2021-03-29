package marketdata.series;

import java.time.Duration;
import java.time.Instant;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import marketdata.container.Bar;
import marketdata.container.Tick;
import utils.TimeSeries;

public class BarData extends MarketDataSeries<Instant,Bar> {

	private TreeMap<Duration,TimeSeries<Instant,Bar>> barDataSeries;
	
	public BarData() {
		super();
	}

	public TreeMap<Duration,TimeSeries<Instant,Bar>> getBarDataSeries() {
		if(this.barDataSeries == null)
			this.barDataSeries = new TreeMap<Duration,TimeSeries<Instant,Bar>>();
		
		return this.barDataSeries;
	}
	
	public void setBarDataSeries(TreeMap<Duration, TimeSeries<Instant, Bar>> barDataSeries) {
		this.barDataSeries = barDataSeries;
	}

	@Override
	public TimeSeries<Instant,Bar> getTimeSeries() {
		return this.getTimeSeries(1);
	}
	
	public TimeSeries<Instant,Bar> getTimeSeries(Integer interval) {
		Duration intervalDuration = Duration.ofMinutes(interval);
		TimeSeries<Instant,Bar> ts;
		if(!this.getBarDataSeries().containsKey(intervalDuration)) {
			ts = new TimeSeries<Instant,Bar>();
			this.getBarDataSeries().put(intervalDuration, ts);
		}
		
		ts = this.getBarDataSeries().get(intervalDuration);
		return ts;
	}
	
	public Bar getBar(Instant start,Instant end) {
		Integer interval = (int) Duration.between(start, end).toMinutes();
		TimeSeries<Instant,Bar> ts = this.getTimeSeries(interval);
		Optional<Entry<Instant,Bar>> filtered = ts.entrySet().stream().filter(entry -> entry.getValue().getStart().equals(start)).findFirst();
		if(filtered.isPresent()) {
			Bar bar = filtered.get().getValue();
			return bar;
		}
		else {
			Bar bar = new Bar(start,end);
			ts.put(end, bar);
			return bar;
		}
	}

	
	public void printTimeSeries(Integer interval) {
		this.getTimeSeries(interval)
		.entrySet()
		.stream()
		.map(x -> x.getKey().toString() + " " + x.getValue().getFieldsMap().toString())
		.forEach(x -> System.out.println(x));
	}
	
}
