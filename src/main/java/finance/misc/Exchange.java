package finance.misc;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import config.CoreConfig;
import event.events.TradingSessionEvent;
import exceptions.TradingSessionMissingException;

public class Exchange {
	private String exchangeCode;
	private String compositeExchangeCode;
	private String mic;
	private Map<TradingPhaseEnum,TradingSession> tradingSessions;
	private ZoneId dateTimeZone;
	private String mainTradingCurrency;
	private TradingSessionEvent lastSessionEvent; // TODO what is it for ?
	private String countryIso;
	
	public Exchange() {
	}
	
	public Exchange(String mic) {
		this.mic = mic;
	}

	public String getMic() {
		return mic;
	}

	public void setMic(String mic) {
		this.mic = mic;
	}

	public Map<TradingPhaseEnum, TradingSession> getTradingSessions() {
		if(tradingSessions == null)
			tradingSessions = new HashMap<TradingPhaseEnum,TradingSession>();
		return tradingSessions;
	}

	public void setTradingSessions(Map<TradingPhaseEnum, TradingSession> tradingSessions) {
		this.tradingSessions = tradingSessions;
	}

	public ZoneId getDateTimeZone() {
		return dateTimeZone;
	}

	public void setDateTimeZone(ZoneId dateTimeZone) {
		this.dateTimeZone = dateTimeZone;
	}

	public String getMainTradingCurrency() {
		return mainTradingCurrency;
	}

	public void setMainTradingCurrency(String mainTradingCurrency) {
		this.mainTradingCurrency = mainTradingCurrency;
	}

	public TradingSessionEvent getLastSessionEvent() {
		return lastSessionEvent;
	}

	public void setLastSessionEvent(TradingSessionEvent lastSessionEvent) {
		this.lastSessionEvent = lastSessionEvent;
	}

	public String getCountryIso() {
		return countryIso;
	}

	public void setCountryIso(String countryIso) {
		this.countryIso = countryIso;
	}

	public String getExchangeCode() {
		return exchangeCode;
	}

	public void setExchangeCode(String exchangeCode) {
		this.exchangeCode = exchangeCode;
	}

	public String getCompositeExchangeCode() {
		if(this.compositeExchangeCode != null) // TODO: throw error instead
			return compositeExchangeCode;
		else
			return this.exchangeCode;
	}

	public void setCompositeExchangeCode(String compositeExchangeCode) {
		this.compositeExchangeCode = compositeExchangeCode;
	}

	public static Exchange getExchangeFromMic(String mic) {
		return CoreConfig.services().getExchange(mic);
	}
	
	public TradingSession getTradingSession(TradingPhaseEnum phase) throws TradingSessionMissingException {
		if(!this.getTradingSessions().containsKey(phase))
			throw new TradingSessionMissingException(phase.name() + " is missing from exchange mic " + this.getMic());
		else
			return this.getTradingSessions().get(phase);
	}
}
