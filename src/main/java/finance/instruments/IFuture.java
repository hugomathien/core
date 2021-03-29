package finance.instruments;

import java.time.LocalDate;

public interface IFuture {
	public int getRollMonthFrequency();
	public LocalDate getExpiryDate();
	public String getUnderlyingIndex();
}
