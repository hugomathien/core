package finance.instruments;


import config.CoreConfig;
import finance.identifiers.IdentifierType;


public class SingleStock extends Instrument {
	
	public SingleStock() {
		this.setInstrumentType(InstrumentType.SingleStock);
	}
	
	public static class Builder extends Instrument.Builder<SingleStock> {
		public Builder() {
			super();
		}
		
		@Override
		public SingleStock build() {
			return (SingleStock) CoreConfig.services().instrumentFactory()
					.makeInstrument(
							InstrumentType.SingleStock,
							this.identifiers.keySet().toArray(new IdentifierType[0]),
							this.identifiers.values().toArray(new String[0]));							
		}
	}

}