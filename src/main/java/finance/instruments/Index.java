package finance.instruments;

import java.time.Instant;
import java.util.HashMap;
import java.util.TreeSet;

import config.CoreConfig;
import event.events.PortfolioCompositionEvent;
import finance.identifiers.IdentifierType;

public class Index extends Instrument implements IPortfolio {
	private IPortfolio portfolio;
	
	public Index() {
		this.portfolio = new Portfolio();
		this.setInstrumentType(InstrumentType.Index);
	}
	
	public static class Builder extends Instrument.Builder<Index> {
		public Builder() {
			super();
		}
		
		@Override
		public Index build() {
			return (Index) CoreConfig.services().instrumentFactory()
					.makeInstrument(
							InstrumentType.Index,
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

	public TreeSet<IInstrument> getHistoricalComposition() {
		return this.portfolio.getHistoricalComposition();
	}

	public void setHistoricalComposition(TreeSet<IInstrument> historicalComposition) {
		this.portfolio.setHistoricalComposition(historicalComposition);
	}

	public void addMember(IInstrument instrument) {
		portfolio.addMember(instrument);
	}
	
	public void addMember(IInstrument instrument,Double weight) {
		portfolio.addMember(instrument,weight);
	}

	@Override
	public Instant getLastCompositionUpdate() {
		return this.portfolio.getLastCompositionUpdate();
	}

	@Override
	public void setLastCompositionUpdate(Instant lastCompositionUpdate) {
		this.setLastCompositionUpdate(lastCompositionUpdate);
	}

	@Override
	public void updateComposition(PortfolioCompositionEvent event) {
		this.portfolio.updateComposition(event);
	}
	
}