package event.sequencing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;
import config.CoreConfig;
import event.events.TimerEvent;
import event.sequencing.processing.CoreEventType;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.enumeration.RequestOverrides;

public class DataRequestSequencer<K> extends AbstractEventSequencer<DataRequest<K>> {
	private DataServiceEnum dataService;
	private RequestType requestType;
	private InstrumentType instrumentType;
	private IdentifierType identifierType = CoreConfig.PRIMARY_IDENTIFIER_TYPE;
	private String[] identifiers;
	private String[] fields;
	private String[] universe;
	private Map<RequestParameters,Object> parameters;
	private Map<RequestOverrides,Object> overrides;
	private boolean backfill = false;
	private boolean subscribe = false;

	public DataRequestSequencer() {
		super();
		this.parameters = new HashMap<RequestParameters,Object>();
		this.overrides = new HashMap<RequestOverrides,Object>();
	}
	
	public DataRequestSequencer(Builder<K> builder) {
		super(builder);
		this.dataService = builder.dataService;
		this.requestType = builder.requestType;
		this.identifierType = builder.identifierType;
		this.instrumentType = builder.instrumentType;
		this.identifiers = builder.identifiers;
		this.fields = builder.fields;
		this.universe = builder.universe;
		this.overrides = builder.overrides;
		this.parameters = builder.parameters;
		this.backfill = builder.backfill;
		this.subscribe = builder.subscribe;
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
				.universe(universe)
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
				.universe(universe)
				.override(overrides) // TODO: update any override datetime parameter to the timer event start/end datetime
				.parameters(parameters)
				.subscribe(subscribe)
				.backfill(backfill)
				.build();
		return request;
	}
	
	private void updateDateTimeOverrides(Temporal start,Temporal end) {
		if(overrides.containsKey(RequestOverrides.START_DATE_OVERRIDE))		
			overrides.put(RequestOverrides.START_DATE_OVERRIDE, LocalDate.from(start));	
		if(overrides.containsKey(RequestOverrides.END_DATE_OVERRIDE))		
			overrides.put(RequestOverrides.END_DATE_OVERRIDE, LocalDate.from(end));	
	}

	public DataServiceEnum getDataService() {
		return dataService;
	}

	public void setDataService(DataServiceEnum dataService) {
		this.dataService = dataService;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public InstrumentType getInstrumentType() {
		return instrumentType;
	}

	public void setInstrumentType(InstrumentType instrumentType) {
		this.instrumentType = instrumentType;
	}

	public IdentifierType getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(IdentifierType identifierType) {
		this.identifierType = identifierType;
	}

	public String[] getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(String[] identifiers) {
		this.identifiers = identifiers;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public String[] getUniverse() {
		return universe;
	}

	public void setUniverse(String[] universe) {
		this.universe = universe;
	}

	public Map<RequestParameters, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<RequestParameters, Object> parameters) {
		this.parameters = parameters;
	}

	public Map<RequestOverrides, Object> getOverrides() {
		return overrides;
	}

	public void setOverrides(Map<RequestOverrides, Object> overrides) {
		this.overrides = overrides;
	}

	public boolean isBackfill() {
		return backfill;
	}

	public void setBackfill(boolean backfill) {
		this.backfill = backfill;
	}

	public boolean isSubscribe() {
		return subscribe;
	}

	public void setSubscribe(boolean subscribe) {
		this.subscribe = subscribe;
	}

	public static class Builder<K> extends AbstractEventSequencer.Builder<DataRequest<K>> {
		private DataServiceEnum dataService;
		private RequestType requestType;
		private InstrumentType instrumentType;
		private IdentifierType identifierType = CoreConfig.PRIMARY_IDENTIFIER_TYPE;
		private String[] identifiers;
		private String[] fields;
		private String[] universe;
		private Map<RequestParameters,Object> parameters;
		private Map<RequestOverrides,Object> overrides;
		private boolean backfill = false;
		private boolean subscribe = false;
		
		public Builder() {
			super();
			parameters = new HashMap<RequestParameters,Object>();
			overrides = new HashMap<RequestOverrides,Object>();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public DataRequestSequencer<K> build() {
			return CoreConfig.ctx.getBean(DataRequestSequencer.class,this);
		}

		public Builder<K> fields(String... fields) {
			this.fields = fields;
			return this;
		}
		
		public Builder<K> universe(String... universe) {
			this.universe = universe;
			return this;
		}

		public Builder<K> parameters(Map<RequestParameters,Object> parameters) {
			this.parameters.putAll(parameters);
			return this;
		}

		public Builder<K> parameters(RequestParameters key,Object value) {
			this.parameters.put(key,value);
			return this;
		}

		public Builder<K> parameters(String key,Object value) {
			this.parameters.put(RequestParameters.valueOf(key),value);
			return this;
		}

		public Builder<K> override(Map<RequestOverrides,Object> overrides) {
			this.overrides.putAll(overrides);
			return this;
		}

		public Builder<K> override(RequestOverrides key, Object value) {
			this.overrides.put(key,value);
			return this;
		}

		public Builder<K> identifierType(IdentifierType idType) {
			this.identifierType = idType;
			return this;
		}

		public Builder<K> instrumentType(InstrumentType instrumentType) {
			this.instrumentType = instrumentType;
			return this;
		}
		
		public Builder<K> identifiers(String... identifiers) {
			this.identifiers = identifiers;
			return this;
		}

		public Builder<K> backfill(boolean backfill) {
			this.backfill = backfill;
			return this;
		}

		public Builder<K> subscribe(boolean subscribe) {
			this.subscribe = subscribe;
			return this;
		}

		public Builder<K> dataService(DataServiceEnum dataService) {
			this.dataService = dataService;
			return this;
		}

		public Builder<K> requestType(RequestType requestType) {
			this.requestType = requestType;
			return this;
		}
		
	}
	
}
