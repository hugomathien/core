package marketdata.services.bloomberg.enumeration;

public enum RequestOverrides {
	fieldID,
	currencyCode,
	vwap_dt,
	vwap_end_time,
	vwap_start_time,
	vwap_participation_rate,
	vwap_target_volume,
	CashAdjNormal,
	CashAdjAbnormal,
	CapChg,
	BEST_FPERIOD_OVERRIDE,
	END_DATE_OVERRIDE;
	
	private RequestOverrides() {
		
	}
}
