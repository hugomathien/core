package finance.instruments;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import config.CoreConfig;
import event.events.PortfolioCompositionEvent;
import finance.identifiers.IdentifierType;

public class ETF extends Instrument implements IPortfolio {
	private IPortfolio portfolio;
	
	public ETF() {
		this.portfolio = new Portfolio();
		this.setInstrumentType(InstrumentType.ETF);
	}
	
	public static class Builder extends Instrument.Builder<ETF> {
		public Builder() {
			super();
		}
		
		@Override
		public ETF build() {
			return (ETF) CoreConfig.services().instrumentFactory()
					.makeInstrument(
							InstrumentType.ETF,
							this.identifiers.keySet().toArray(new IdentifierType[0]),
							this.identifiers.values().toArray(new String[0]));							
		}
	}

	public IPortfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(IPortfolio portfolio) {
		this.portfolio = portfolio;
	}
	
	public HashMap<IInstrument, Double> getWeights() {
		return portfolio.getWeights();
	}
	
	public void setWeights(HashMap<IInstrument, Double> weights) {
		portfolio.setWeights(weights);
	}
	
	public TreeSet<IInstrument> getComposition() {
		return portfolio.getComposition();
	}
	
	public void setComposition(TreeSet<IInstrument> composition) {
		portfolio.setComposition(composition);
	}

	public void addMember(IInstrument instrument) {
		portfolio.addMember(instrument);
	}
	
	public void addMember(IInstrument instrument,Double weight) {
		portfolio.addMember(instrument,weight);
	}

	@Override
	public Instant getLastCompositionUpdate() {
		return this.getLastCompositionUpdate();
	}

	@Override
	public void setLastCompositionUpdate(Instant lastCompositionUpdate) {
		this.setLastCompositionUpdate(lastCompositionUpdate);
	}

	public TreeSet<IInstrument> getHistoricalComposition() {
		return this.portfolio.getHistoricalComposition();
	}

	public void setHistoricalComposition(TreeSet<IInstrument> historicalComposition) {
		this.portfolio.setHistoricalComposition(historicalComposition);
	}

	@Override
	public void updateComposition(PortfolioCompositionEvent event) {
		this.portfolio.updateComposition(event);
	}

}