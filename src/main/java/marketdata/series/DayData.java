package marketdata.series;

import java.time.LocalDate;

import marketdata.container.Day;
import utils.TimeSeries;

public class DayData extends MarketDataSeries<LocalDate,Day> {

	private TimeSeries<LocalDate,Day> eodSeries;
	
	public DayData() {
		super();
	}
	
	@Override
	public TimeSeries<LocalDate, Day> getTimeSeries(Integer interval) {
		return this.eodSeries;
	}

	@Override
	public TimeSeries<LocalDate, Day> getTimeSeries() {
		if(this.eodSeries == null)
			this.eodSeries = new TimeSeries<LocalDate,Day>();
		
		return this.eodSeries;
	}
	
	public Day getEod(LocalDate timestamp) {
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

}
