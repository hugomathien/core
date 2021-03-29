package finance.instruments;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Predicate;

import config.CoreConfig;
import finance.identifiers.IdentifierType;

public class Future extends Instrument implements IFuture {
	private int rollMonthFrequency = 3;
	private LocalDate expiryDate;
	private String underlyingIndex; // TODO make it Index object
	
	public Future() {
		this.setInstrumentType(InstrumentType.Future);
	}

	public int getRollMonthFrequency() {
		return rollMonthFrequency;
	}

	public void setRollMonthFrequency(int rollMonthFrequency) {
		this.rollMonthFrequency = rollMonthFrequency;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getUnderlyingIndex() {
		return underlyingIndex;
	}

	public void setUnderlyingIndex(String underlyingIndex) {
		this.underlyingIndex = underlyingIndex;
	}
	
	public Future getContract(Predicate<Future> filter) {
		return getContract(filter,this.underlyingIndex);
	}
	
	public Future getNextContract() {
		return getContract(x -> x.getExpiryDate().isAfter(expiryDate));
	}
	
	public Future getPreviousContract() {
		return getContract(x -> x.getExpiryDate().isBefore(expiryDate));
	}
	

	public static Future getContract(Predicate<Future> filter,String underlyingIndex) {
		Future fut = CoreConfig.
				services().instrumentFactory().getInstrumentSet()
				.stream().filter(x -> x instanceof Future)
				.map(x -> (Future) x)
				.filter(x -> x.getUnderlyingIndex().equals(underlyingIndex))
				.filter(filter)
				.sorted(Comparator.comparing(Future::getExpiryDate))
				.findFirst()
				.orElse(null);
		return fut;
	}
	
	public static Future getFrontMonthContractForIndex(String underlyingIndex,LocalDate date,int expiryDayOffset) {
		return getContract(x -> x.getExpiryDate().minusDays(expiryDayOffset).isAfter(date),underlyingIndex);
	}

	public static class Builder extends Instrument.Builder<Future> {
		public Builder() {
			super();
		}
		
		@Override
		public Future build() {
			return (Future) CoreConfig.services().instrumentFactory()
					.makeInstrument(
							InstrumentType.Future,
							this.identifiers.keySet().toArray(new IdentifierType[0]),
							this.identifiers.values().toArray(new String[0]));
		}
	}
	

}

