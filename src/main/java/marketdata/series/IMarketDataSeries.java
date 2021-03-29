package marketdata.series;

import java.time.temporal.Temporal;

import marketdata.container.AbstractMarketDataContainer;
import utils.TimeSeries;

public interface IMarketDataSeries<T extends Temporal,C extends AbstractMarketDataContainer> {
	public TimeSeries<T,C> getTimeSeries(Integer interval);
	public TimeSeries<T,C> getTimeSeries();
	default public void clear() {
		this.getTimeSeries().clear();
	}
	
}
