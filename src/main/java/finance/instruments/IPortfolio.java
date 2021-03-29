package finance.instruments;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface IPortfolio {
	public HashMap<IInstrument, Double> getWeights();
	public void setWeights(HashMap<IInstrument, Double> weights);
	public List<IInstrument> getComposition();
	public void setComposition(List<IInstrument> composition);
	public void addMember(IInstrument instrument);
	public void addMember(IInstrument instrument,Double weight);
	public Instant getLastCompositionUpdate();
	public void setLastCompositionUpdate(Instant lastCompositionUpdate);
}
