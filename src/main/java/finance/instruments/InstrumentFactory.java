package finance.instruments;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import config.CoreConfig;
import events.InstrumentDelistEvent;
import events.NewInstrumentEvent;
import finance.identifiers.IIdentifier;
import finance.identifiers.Identifier;
import finance.identifiers.IdentifierType;

import static finance.instruments.InstrumentType.*;
import finance.identifiers.*;

@Service
@Scope("singleton")
@Lazy(true)
public class InstrumentFactory {
	@Autowired
	public ApplicationContext ctx;
	@Autowired
	public CoreConfig config;
	@Autowired
	private ApplicationEventPublisher publisher;
	private Set<IInstrument> instrumentSet = new HashSet<IInstrument>(); // TODO: Needs concurrent ? Key and improve lookup time
	private Set<IIdentifier> identifierSet = new HashSet<IIdentifier>(); // Needs concurrent ? TODO: Key and improve lookup time

	public InstrumentFactory() {}

	public Set<IInstrument> getInstrumentSet() {
		return instrumentSet;
	}
	
	public void setInstrumentSet(Set<IInstrument> instrumentSet) {
		this.instrumentSet = instrumentSet;
	}

	public Set<IIdentifier> getIdentifierSet() {
		return identifierSet;
	}

	public void setIdentifierSet(Set<IIdentifier> identifierSet) {
		this.identifierSet = identifierSet;
	}

	private Iterator<String> getIterator(String[] names) {
		List<String> listNames = Arrays.asList(names);
		return listNames.iterator();
	}

	private Iterator<String> getIterator(Collection<String> names) {
		return names.iterator();
	}

	public IIdentifier getIdentifier(String securityName) {
		return this.identifierSet.stream().filter(i -> i.getName().equals(securityName)).findAny().orElse(null);
	}

	public IInstrument getInstrument(String securityName) {
		if(securityName == null)
			return null;
		try {
			IIdentifier identifier = this.identifierSet.stream().filter(i -> i.getName().equals(securityName)).findAny().get();
			return identifier.getInstrument();
		}
		catch(NoSuchElementException e) {
			return null;
		}
	}

	public boolean hasInstrument(String securityName) {
		return this.identifierSet.stream().anyMatch(i -> i.getName().equals(securityName));
	}

	public boolean hasInstrument(String securityName,InstrumentType instrumentType) {
		return this.identifierSet.stream().anyMatch(i -> i.getInstrument().getInstrumentType().equals(instrumentType) && i.getName().equals(securityName));
	}

	public boolean hasFx(String ccyLeft,String ccyRight) {
		return this.instrumentSet.stream().filter(i -> i.getInstrumentType().equals(InstrumentType.FX)).anyMatch(i -> ((FX) i).getCcyLeft().equals(ccyLeft) && ((FX) i).getCcyRight().equals(ccyRight));
	}

	public boolean removeInstrument(IInstrument instrument,InstrumentType instrumentType,LocalDate... date) {
		try {
			List<IIdentifier> identifiersToRemove = this.identifierSet.stream().filter(x -> x.getInstrument().equals(instrument)).collect(Collectors.toList());
			this.identifierSet.removeAll(identifiersToRemove);
			this.instrumentSet.remove(instrument);
			
			if(date.length>0) {
				publisher.publishEvent(
						new InstrumentDelistEvent (
								date[0].atStartOfDay(ZoneId.systemDefault()).toInstant(),
								instrument,
								instrumentType));
			}
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	public IInstrument makeInstrument(
			InstrumentType instrumentType,
			List<IdentifierType> identifierTypes,
			List<String> names,
			LocalDate... date) {

		IInstrument instrument = null;
		Iterator<String> nameIterator = names.iterator();
		while(nameIterator.hasNext() && instrument == null)
			instrument = this.getInstrument(nameIterator.next());

		if(instrument == null)
			instrument = makeInstrument(instrumentType,identifierTypes.get(0),names.get(0),date);

		for(int i = 0; i < names.size(); i++)
			this.addIdentifier(instrument, identifierTypes.get(i), names.get(i));

		return instrument;
	}


	public IInstrument makeInstrument(
			InstrumentType instrumentType,
			IdentifierType[] identifierTypes,
			String[] names,
			LocalDate... date) {
		return makeInstrument(instrumentType,Arrays.asList(identifierTypes),Arrays.asList(names),date);
	}

	public IInstrument makeInstrument(InstrumentType instrumentType,String securityName) {
		IInstrument instrument = this.getInstrument(securityName);
		if(instrument == null) {
			IdentifierType identifierType = instrumentType.equals(FX) ? IdentifierType.guessFX(securityName) : IdentifierType.guess(securityName);
			instrument = makeInstrument(instrumentType,identifierType,securityName);
		}
		return instrument;
	}

	public IInstrument makeInstrument(InstrumentType instrumentType,IdentifierType identifierType,String securityName,LocalDate...date) {
		if(securityName == null)
			return null;
		IInstrument instrument = this.getInstrument(securityName);
		if(instrument == null) {
			switch(instrumentType) {
			case SingleStock:
				instrument = new SingleStock();
				break;
			case Index:
				instrument = new Index();
				break;
			case ETF:
				instrument = new ETF();
				break;
			case Future:
				instrument = new Future();
				break;
			default:
				return null;
			}			
		}

		this.instrumentSet.add(instrument);
		this.addIdentifier(instrument,identifierType,securityName, true);

		if(date.length>0) {
			publisher.publishEvent(
					new NewInstrumentEvent(
							date[0].atStartOfDay(ZoneId.systemDefault()).toInstant(),
							instrument,
							instrumentType));
		}
		return instrument;
	}

	public HashSet<IInstrument> makeMultipleInstrument(InstrumentType instrumentType, String... names) {
		Iterator<String> iterator = getIterator(names);
		return makeMultipleInstrument(instrumentType,iterator);
	}
	
	public HashSet<IInstrument> makeMultipleInstrument(InstrumentType instrumentType, IdentifierType identifierType,String[] names) {
		Iterator<String> iterator = getIterator(names);
		return makeMultipleInstrument(instrumentType,identifierType,iterator);
	}
	
	public HashSet<IInstrument> makeMultipleInstrument(InstrumentType instrumentType, Collection<String> names) {
		Iterator<String> iterator = getIterator(names);
		return makeMultipleInstrument(instrumentType,iterator);
	}
	
	public HashSet<IInstrument> makeMultipleInstrument(InstrumentType instrumentType, IdentifierType identifierType,Collection<String> names) {
		Iterator<String> iterator = getIterator(names);
		return makeMultipleInstrument(instrumentType,identifierType,iterator);
	}
	
	public HashSet<IInstrument> makeMultipleInstrument(InstrumentType instrumentType, Iterator<String> names) {
		HashSet<IInstrument> set = new HashSet<IInstrument>();
		while(names.hasNext()) {
			String name = names.next();
			IInstrument instrument = makeInstrument(instrumentType,name);
			set.add(instrument);
		}
		return set;
	}
	
	public HashSet<IInstrument> makeMultipleInstrument(InstrumentType instrumentType, IdentifierType identifierType, Iterator<String> names) {
		HashSet<IInstrument> set = new HashSet<IInstrument>();
		while(names.hasNext()) {
			String name = names.next();
			IInstrument instrument = makeInstrument(instrumentType,identifierType,name);
			set.add(instrument);
		}
		return set;
	}
	
	public FX getFx(String ccyLeft,String ccyRight) {
		return (FX) this.instrumentSet.stream().filter(i -> i.getInstrumentType().equals(FX)).filter(i -> ((FX) i).getCcyLeft().equals(ccyLeft) && ((FX) i).getCcyRight().equals(ccyRight)).findAny().orElse(null);
	}
	
	public FX makeFx(String ccyLeft,String ccyRight) {
		if(this.hasFx(ccyLeft, ccyRight))
			return getFx(ccyLeft, ccyRight);
		FX fx = new FX(ccyLeft,ccyRight);
		this.instrumentSet.add(fx);
		this.addIdentifier(fx,IdentifierType.TICKER,ccyLeft+ccyRight,true);
		return fx;
	}
	
	private Identifier makeIdentifier(IInstrument instrument,IdentifierType identifierType,String name) {
		Identifier identifier = null;
		
		switch(identifierType) {
		case TICKER:
			identifier = new Ticker(instrument,name);
			break;
		case RIC:
			identifier = new Ric(instrument,name);
			break;
		case SEDOL:
			identifier = new Sedol(instrument,name);
			break;
		}
		return identifier;		
	}
	
	public IInstrument addIdentifier(IInstrument instrument,String name) {
		IdentifierType idType;
		if(instrument instanceof FX)
			idType = IdentifierType.guessFX(name);
		else
			idType = IdentifierType.guess(name);
		return addIdentifier(instrument,idType,name,false);
	}
	
	public IInstrument addIdentifier(IInstrument instrument,IdentifierType idType,String name) {
		return addIdentifier(instrument,idType,name,false);
	}
	
	public IInstrument addIdentifier(IInstrument instrument,IdentifierType idType,String name,boolean makePrimary) {
		Identifier id = makeIdentifier(instrument,idType,name);
		instrument.getIdentifiers().add(id);
		this.identifierSet.add(id);
		if(makePrimary)
			instrument.setPrimaryIdentifierType(idType);
		return instrument;
	}
	
	
}
