package marketdata.series;

import java.time.Instant;
import marketdata.container.Tick;
import utils.TimeSeries;

public class TickData extends MarketDataSeries<Instant,Tick> {

	private TimeSeries<Instant,Tick> tickDataSeries;
	
	public TickData() {
		super();
	}
	
	@Override
	public TimeSeries<Instant, Tick> getTimeSeries(Integer interval) {
		return this.tickDataSeries;
	}

	@Override
	public TimeSeries<Instant, Tick> getTimeSeries() {
		if(this.tickDataSeries == null)
			this.tickDataSeries = new TimeSeries<Instant,Tick>();
		
		return this.tickDataSeries;
	}
	
	public Tick getTick(Instant timestamp) {
		Tick tick;
		if(this.getTimeSeries().containsKey(timestamp)) {
			tick = this.getTimeSeries().get(timestamp);
		}
		else {
			tick = new Tick(timestamp);
			this.getTimeSeries().put(timestamp, tick);
		}
		return tick;
	}

}
