package finance.instruments;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import config.CoreConfig;
import finance.identifiers.IdentifierType;

public class FX extends Instrument {
	
	@Autowired
	private InstrumentFactory factory;
	@Autowired
	private CoreConfig config;
	private String ccyLeft;
	private String ccyRight;
	
	public FX(String left,String right) {
		this.ccyLeft = left;
		this.ccyRight = right;
		this.setCurrency(left);
		this.setInstrumentType(InstrumentType.FX);
	}

	@PostConstruct
	public void init() {	
	}

	public String getCcyLeft() {
		return ccyLeft;
	}

	public void setCcyLeft(String ccyLeft) {
		this.ccyLeft = ccyLeft;
	}

	public String getCcyRight() {
		return ccyRight;
	}

	public void setCcyRight(String ccyRight) {
		this.ccyRight = ccyRight;
	}
	
	@Override
	public String toString() {
		return this.ccyLeft + this.ccyRight;
	}
	
	public static class Builder extends Instrument.Builder<FX> {
		@Autowired
		private InstrumentFactory factory;
		private String ccyLeft;
		private String ccyRight;
		
		public Builder() {
			super();
		}
		
		public Builder ccyLeft(String ccyLeft) {
			this.ccyLeft = ccyLeft;
			return this;
		}
		
		public Builder ccyRight(String ccyRight) {
			this.ccyRight = ccyRight;
			return this;
		}
		
		@Override
		public FX build() {
			if(this.ccyLeft != null && this.ccyRight != null) {
				return (FX) factory.makeFx(this.ccyLeft,this.ccyRight);
			}
			else {
				return (FX) factory.makeInstrument(
						InstrumentType.FX,
						this.identifiers.keySet().toArray(new IdentifierType[0]),
						this.identifiers.values().toArray(new String[0]));						
			}
		}
	}
}
