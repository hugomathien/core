package finance.instruments;

import java.time.Instant;
import java.util.HashMap;
import java.util.TreeSet;

public interface IPortfolio {
	public HashMap<IInstrument, Double> getWeights();
	public void setWeights(HashMap<IInstrument, Double> weights);
	public TreeSet<IInstrument> getComposition();
	public void setComposition(TreeSet<IInstrument> composition);
	public void addMember(IInstrument instrument);
	public void addMember(IInstrument instrument,Double weight);
	public Instant getLastCompositionUpdate();
	public void setLastCompositionUpdate(Instant lastCompositionUpdate);
}
