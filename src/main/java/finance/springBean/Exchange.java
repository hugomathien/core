package finance.springBean;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import config.CoreConfig;
import events.TradingSessionEvent;
import exceptions.TradingSessionMissingException;
import finance.tradingcycle.TradingPhaseEnum;

public class Exchange {
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
