package event.events;

import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import event.sequencing.processing.CoreEventType;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentType;

@Component
@Scope("prototype")
@Lazy(true)
public class InstrumentDelistEvent extends Event {
	private IInstrument instrument;
	private InstrumentType instrumentType;
	
	public InstrumentDelistEvent(Instant eventTimestamp,IInstrument instrument, InstrumentType instrumentType) {
		super(eventTimestamp,CoreEventType.DELIST_INSTRUMENT);
		this.instrument = instrument;
		this.instrumentType = instrumentType;
	}
	
	public IInstrument getInstrument() {
		return instrument;
	}
	
	public void setInstrument(IInstrument instrument) {
		this.instrument = instrument;
	}

	public InstrumentType getInstrumentType() {
		return instrumentType;
	}

	public void setInstrumentType(InstrumentType instrumentType) {
		this.instrumentType = instrumentType;
	}
}
