package finance.instruments;

import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

// TODO not sure it's used anywhere atm
@Component
@Scope("prototype")
public class InstrumentPair extends Pair<Instrument,Instrument> {
	
	public InstrumentPair(Instrument k,Instrument v) {
		super(k, v);
	}

}
