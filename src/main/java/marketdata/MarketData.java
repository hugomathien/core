package marketdata;


import marketdata.container.Spot;

public class MarketData {

	private Spot spot;
	private HistoricalData historical;
	
	public MarketData() {
		
	}
	
	public Spot getSpot() {
		if(this.spot == null)
			this.spot = new Spot();
		return spot;
	}
	
	public void setSpot(Spot spot) {
		this.spot = spot;
	}
	
	public HistoricalData getHistorical() {
		if(this.historical == null)
			this.historical = new HistoricalData();
		return historical;
	}
	
	public void setHistorical(HistoricalData historical) {
		this.historical = historical;
	}
}
