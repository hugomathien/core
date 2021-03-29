package events;

import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eventprocessors.CoreEventType;
import finance.instruments.IInstrument;

@Component
@Scope("prototype")
@Lazy(true)
public class SplitEvent extends Event {
	private double splitFactor;
	private IInstrument instrument;
	
	public SplitEvent(Instant eventTimestamp,IInstrument instrument, double value) {
		super(eventTimestamp,CoreEventType.SPLIT);
		this.splitFactor = value;
		this.instrument = instrument;
	}
	
	public double getSplitFactor() {
		return splitFactor;
	}
	
	public void setSplitFactor(double splitFactor) {
		this.splitFactor = splitFactor;
	}
	
	public IInstrument getInstrument() {
		return instrument;
	}
	
	public void setInstrument(IInstrument instrument) {
		this.instrument = instrument;
	}
}
