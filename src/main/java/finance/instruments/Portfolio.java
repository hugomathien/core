package finance.instruments;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class Portfolio implements IPortfolio {
	private HashMap<IInstrument,Double> weights;
	private TreeSet<IInstrument> composition;
	private Instant lastCompositionUpdate;
	
	public Portfolio() {
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
	}

	public Instant getLastCompositionUpdate() {
		return lastCompositionUpdate;
	}

	public void setLastCompositionUpdate(Instant lastCompositionUpdate) {
		this.lastCompositionUpdate = lastCompositionUpdate;
	}
	
	
}
