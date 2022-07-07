package event.events;

import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import event.sequencing.processing.CoreEventType;
import finance.instruments.IInstrument;

@Component
@Scope("prototype")
@Lazy(true)
public class DividendEvent extends Event {
	private double dividend;
	private IInstrument instrument;
	
	public DividendEvent(Instant eventTimestamp,IInstrument instrument, double value) {
		super(eventTimestamp,CoreEventType.DIVIDEND);
		this.dividend = value;
		this.instrument = instrument;
	}
	
	public double getDividend() {
		return dividend;
	}
	
	public void setDividend(double dividend) {
		this.dividend = dividend;
	}
	
	public IInstrument getInstrument() {
		return instrument;
	}
	
	public void setInstrument(IInstrument instrument) {
		this.instrument = instrument;
	}
}
