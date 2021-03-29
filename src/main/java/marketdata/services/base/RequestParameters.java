package marketdata.services.base;

public enum RequestParameters {
	startDateTime,
	endDateTime,
	startDate,
	endDate,
	startTime,
	endTime,
	nonTradingDayFillOption,
	periodicitySelection,
	nonTradingDayFillMethod,
	interval,
	intervalType,
	gapFillInitialBar,
	UseDFDF,
	Use1DDateList,
	indices,
	zoneId,
	books,
	includeNonPlottableEvents,
	includeConditionCodes,
	includeExchangeCodes,
	includeBrokerCodes,
	includeRpsCode,
	includeBicMicCodes,
	minVolumePct;
	
	private RequestParameters() {
		
	}
}
