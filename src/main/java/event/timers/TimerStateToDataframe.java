package event.timers;

import java.time.Instant;
import java.time.temporal.Temporal;

import config.CoreConfig;
import event.events.TimerEvent;
import event.processing.CoreEventType;

public class TimerStateToDataframe extends AbstractEventSequencer<dataset.StateToDataframe>  {
	private dataset.StateToDataframe stateCapture;

	public TimerStateToDataframe() {
		super();
	}

	public TimerStateToDataframe(TimerStateToDataframe.Builder builder) {
		super(builder);
		this.stateCapture = builder.stateCapture;
	}

	@Override
	public TimerEvent createEvent(Instant eventTimestamp, Temporal start, Temporal end) {
		TimerEvent te = new TimerEvent(this,eventTimestamp,stateCapture,CoreEventType.INSTRUMENT_STATE_CAPTURE,0);
		return te;
	}

	public static class Builder extends AbstractEventSequencer.Builder<dataset.StateToDataframe> {
		private dataset.StateToDataframe stateCapture;
		public Builder() {
			super();
		}

		@SuppressWarnings("unchecked")
		@Override
		public AbstractEventSequencer<dataset.StateToDataframe> build() {
			return CoreConfig.ctx.getBean(TimerStateToDataframe.class,this);
		}

		public Builder stateCapture(dataset.StateToDataframe stateCapture) {
			this.stateCapture = stateCapture;
			return this;
		}
	}

}
