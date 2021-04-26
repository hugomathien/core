package eventsequencers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;

import config.CoreConfig;
import eventprocessors.CoreEventType;
import events.TimerEvent;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.enumeration.RequestOverrides;

public class DataRequestEventSequencer<K> extends AbstractEventSequencer {
	private DataServiceEnum dataService;
	private RequestType requestType;
	private InstrumentType instrumentType;
	private IdentifierType identifierType = CoreConfig.PRIMARY_IDENTIFIER_TYPE;
	private String[] identifiers;
	private String[] fields;
	private Map<RequestParameters,Object> parameters;
	private Map<RequestOverrides,Object> overrides;
	private boolean backfill = false;
	private boolean subscribe = false;
	private boolean defaultAllId = false;

	public DataRequestEventSequencer() {
		this.parameters = new HashMap<RequestParameters,Object>();
		this.overrides = new HashMap<RequestOverrides,Object>();
	}
	
	@Override
	public TimerEvent createEvent(Instant eventTimestamp,Temporal start,Temporal end) {
		DataRequest<K> request;
		switch(requestType) {
		case ReferenceDataRequest:
			request = referenceDataRequest(start,end);
			break;
		default:
			request = historicalDataRequest(start,end);
			break;
		}
		
		TimerEvent te = new TimerEvent(this,eventTimestamp,request,CoreEventType.DATA_REQUEST);
		return te;
	}
	
	private DataRequest<K> historicalDataRequest(Temporal start,Temporal end) {
		DataRequest<K> request = new DataRequest.Builder<K>()
				.dataService(dataService)
				.requestType(requestType)
				.instrumentType(instrumentType)
				.identifierType(identifierType)
				.identifiers(instrumentType,identifiers)
				.fields(fields)
				.override(overrides)
				.parameters(parameters)
				.parameters((start instanceof ZonedDateTime) ? RequestParameters.startDateTime : RequestParameters.startDate,start)
				.parameters((end instanceof ZonedDateTime) ? RequestParameters.endDateTime : RequestParameters.endDate,end)
				.subscribe(subscribe)
				.backfill(backfill)
				.build();
		return request;
	}
	
	private DataRequest<K> referenceDataRequest(Temporal start,Temporal end) {
		updateDateTimeOverrides(start,end);
		DataRequest<K> request = new DataRequest.Builder<K>()
				.dataService(dataService)
				.requestType(requestType)
				.instrumentType(instrumentType)
				.identifierType(identifierType)
				.identifiers(instrumentType,identifiers)
				.fields(fields)
				.override(overrides) // TODO: update any override datetime parameter to the timer event start/end datetime
				.parameters(parameters)
				.subscribe(subscribe)
				.backfill(backfill)
				.build();
		return request;
	}
	
	private void updateDateTimeOverrides(Temporal start,Temporal end) {
		if(overrides.containsKey(RequestOverrides.END_DATE_OVERRIDE))		
			overrides.put(RequestOverrides.END_DATE_OVERRIDE, LocalDate.from(end));	
	}
}
