package finance.identifiers;


import finance.instruments.IInstrument;
import finance.misc.Exchange;

public abstract class Identifier implements Comparable<Identifier> {

	private String name;
	private IdentifierType type;
	private IInstrument instrument;
	
	public Identifier(IdentifierType type,String name) {
		this.name = name;
		this.type = type;
	}
	
	public Identifier(IInstrument instrument,String name,IdentifierType type) {
		this.instrument = instrument;
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IdentifierType getType() {
		return type;
	}

	public void setType(IdentifierType type) {
		this.type = type;
	}

	public IInstrument getInstrument() {
		return instrument;
	}

	public void setInstrument(IInstrument instrument) {
		this.instrument = instrument;
	}
	
	public String toString() {
		return this.getName().toString();
	}
	
	public abstract Exchange guessExchange();
	
	@Override
	public int compareTo(Identifier identifier) {
		return this.getName().compareTo(identifier.getName());
		
	}
}
