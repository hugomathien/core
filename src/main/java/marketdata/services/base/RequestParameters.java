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
	UseDPDF,
	Use1DDateList,
	indices,
	zoneId,
	books,
	adjustmentSplit,
	adjustmentAbnormal,
	adjustmentNormal,
	adjustmentFollowDPDF,
	includeNonPlottableEvents,
	includeConditionCodes,
	includeExchangeCodes,
	includeBrokerCodes,
	includeRpsCode,
	includeBicMicCodes,
	minVolumePct,
	randomizedNumberScales;
	
	private RequestParameters() {
		
	}
}
