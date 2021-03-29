package marketdata;

import marketdata.series.BarData;
import marketdata.series.DayData;
import marketdata.series.TickData;


public class HistoricalData {

	private TickData tickData;
	private DayData eodData;
	private BarData barData;
	
	public HistoricalData() {
		
	}
	
	public TickData getTickData() {
		if(this.tickData == null)
			this.tickData = new TickData();
		return tickData;
	}
	
	public void setTickData(TickData tickData) {
		this.tickData = tickData;
	}
	
	public DayData getEodData() {
		if(this.eodData == null)
			this.eodData = new DayData();
		return eodData;
	}
	
	public void setEodData(DayData eodData) {
		this.eodData = eodData;
	}
	
	public BarData getBarData() {
		if(this.barData == null)
			this.barData = new BarData();
		return barData;
	}
	
	public void setBarData(BarData barData) {
		this.barData = barData;
	}
	
	public void clear() {
		this.barData.clear();
		this.tickData.clear();
		this.eodData.clear();
	}
}
