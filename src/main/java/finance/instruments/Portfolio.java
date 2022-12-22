package finance.instruments;

import event.events.PortfolioCompositionEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class Portfolio implements IPortfolio {
	private HashMap<IInstrument,Double> weights;
	private TreeSet<IInstrument> composition;
	private HashMap<IInstrument,Double> previousWeights;
	private TreeSet<IInstrument> previousComposition;
	private TreeSet<IInstrument> historicalComposition;
	private Instant lastCompositionUpdate;
	
	public Portfolio() {
		this.init();
		this.historicalComposition = new TreeSet<IInstrument>();
	}

	private void init() {
		this.weights = new HashMap<IInstrument,Double>();
		this.composition = new TreeSet<IInstrument>();
	}

	public HashMap<IInstrument, Double> getWeights() {
		return weights;
	}
	
	public void setWeights(HashMap<IInstrument, Double> weights) {
		this.weights = weights;
	}
	
	public TreeSet<IInstrument> getComposition() {
		return composition;
	}
	
	public void setComposition(TreeSet<IInstrument> composition) {
		this.composition = composition;
	}

	public void addMember(IInstrument instrument) {
		this.composition.add(instrument);
	}
	
	public void addMember(IInstrument instrument,Double weight) {
		this.composition.add(instrument);
		this.weights.put(instrument, weight);
		this.historicalComposition.add(instrument);
	}

	public Instant getLastCompositionUpdate() {
		return lastCompositionUpdate;
	}

	public void setLastCompositionUpdate(Instant lastCompositionUpdate) {
		this.lastCompositionUpdate = lastCompositionUpdate;
	}

	public HashMap<IInstrument, Double> getPreviousWeights() {
		return previousWeights;
	}

	public void setPreviousWeights(HashMap<IInstrument, Double> previousWeights) {
		this.previousWeights = previousWeights;
	}

	public TreeSet<IInstrument> getPreviousComposition() {
		return previousComposition;
	}

	public void setPreviousComposition(TreeSet<IInstrument> previousComposition) {
		this.previousComposition = previousComposition;
	}

	public TreeSet<IInstrument> getHistoricalComposition() {
		return historicalComposition;
	}

	public void setHistoricalComposition(TreeSet<IInstrument> historicalComposition) {
		this.historicalComposition = historicalComposition;
	}

	public void updateComposition(PortfolioCompositionEvent event) {
		if(lastCompositionUpdate == null || event.getEventTimestamp().isAfter(lastCompositionUpdate)) {
			this.previousWeights = this.weights;
			this.previousComposition = this.composition;
			this.init();
			this.lastCompositionUpdate = event.getEventTimestamp();
		}

		this.addMember(event.getMember(),event.getWeight());
	}
	
}
