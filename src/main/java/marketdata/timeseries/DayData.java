package marketdata.timeseries;

import java.time.Instant;
import java.time.LocalDate;

import config.CoreConfig;
import marketdata.container.Day;
import utils.TimeSeries;

public class DayData extends MarketDataSeries<Instant,Day> {

	private TimeSeries<Instant,Day> eodSeries;
	
	public DayData() {
		super();
	}
	
	@Override
	public TimeSeries<Instant, Day> getTimeSeries(Integer interval) {
		return this.eodSeries;
	}

	@Override
	public TimeSeries<Instant, Day> getTimeSeries() {
		if(this.eodSeries == null)
			this.eodSeries = new TimeSeries<Instant,Day>();
		
		return this.eodSeries;
	}
	
	public Day getEod(Instant timestamp) {
		Day eod;
		if(this.getTimeSeries().containsKey(timestamp)) {
			eod = this.getTimeSeries().get(timestamp);
		}
		else {
			eod = new Day(timestamp);
			this.getTimeSeries().put(timestamp, eod);
		}
		return eod;
	}
	
	public Day getEod(LocalDate ld) {
		Day eod;
		Instant instant = ld.atStartOfDay(CoreConfig.GLOBAL_ZONE_ID).toInstant();
		return getEod(instant);
	}
	
	public Day getEod(String ld) {
		return getEod(LocalDate.parse(ld));
	}

}
