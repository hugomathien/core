package event.timers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;
import config.CoreConfig;
import event.events.TimerEvent;
import event.processing.CoreEventType;
import finance.identifiers.IdentifierType;
import finance.instruments.InstrumentType;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.utils.RequestOverrides;

public class TimerDataRequest extends AbstractEventSequencer<DataRequest> {
	private DataServiceEnum dataService;
	private RequestType requestType;
	private InstrumentType instrumentType;
	private IdentifierType identifierType = CoreConfig.PRIMARY_IDENTIFIER_TYPE;
	private String[] identifiers;
	private String[] fields;
	private String[] universe;
	private Map<RequestParameters,Object> parameters  = new HashMap<RequestParameters,Object>();
	private Map<RequestOverrides,Object> overrides = new HashMap<RequestOverrides,Object>();
	private boolean backfill = false;
	private boolean subscribe = false;

	public TimerDataRequest() {
		super();
	}
	
	public TimerDataRequest(Builder builder) {
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
		DataRequest request;
		DataRequest.Builder requestBuilder = this.buildRequest(start,end);
		switch(requestType) {
			case ReferenceDataRequest:
				request = referenceDataRequest(start,end,requestBuilder);
				break;
			default:
				request = historicalDataRequest(start,end,requestBuilder);
				break;
		}
		
		TimerEvent te = new TimerEvent(this,eventTimestamp,request,CoreEventType.DATA_REQUEST);
		return te;
	}

	private DataRequest.Builder buildRequest(Temporal start, Temporal end) {
		return new DataRequest.Builder()
				.dataService(dataService)
				.requestType(requestType)
				.instrumentType(instrumentType)
				.identifierType(identifierType)
				.identifiers(instrumentType,identifiers)
				.fields(fields)
				.universe(universe)
				.override(overrides)
				.parameters(parameters)
				.subscribe(subscribe)
				.backfill(backfill);
	}

	private DataRequest historicalDataRequest(Temporal start, Temporal end, DataRequest.Builder requestBuilder) {
		DataRequest request = requestBuilder
				.parameters((start instanceof ZonedDateTime) ? RequestParameters.startDateTime : RequestParameters.startDate,start)
				.parameters((end instanceof ZonedDateTime) ? RequestParameters.endDateTime : RequestParameters.endDate,end)
				.build();
		return request;
	}
	
	private DataRequest referenceDataRequest(Temporal start, Temporal end, DataRequest.Builder requestBuilder) {
		updateDateTimeOverrides(start,end);
		DataRequest request = requestBuilder.build();
		return request;
	}

	private void updateDateTimeOverrides(Temporal start,Temporal end) {
		if(overrides.containsKey(RequestOverrides.START_DATE_OVERRIDE))		
			overrides.put(RequestOverrides.START_DATE_OVERRIDE, LocalDate.from(start));	
		if(overrides.containsKey(RequestOverrides.END_DATE_OVERRIDE))		
			overrides.put(RequestOverrides.END_DATE_OVERRIDE, LocalDate.from(end));	
	}

	public String toString() {
		return new StringBuilder()
				.append(super.toString())
				.append("DataService="+this.getDataService().toString())
				.append("RequestType="+this.getRequestType().toString())
				.append("IdentifiersCount="+this.identifiers.length)
				.append("Fields="+this.getFields())
				.append("Parameters="+this.getParameters().toString())
				.append("Overrides="+this.getOverrides().toString()).toString();
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

	public static class Builder extends AbstractEventSequencer.Builder<DataRequest> {
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
		public TimerDataRequest build() {
			return CoreConfig.ctx.getBean(TimerDataRequest.class,this);

		}

		public Builder fields(String... fields) {
			this.fields = fields;
			return this;
		}
		
		public Builder universe(String... universe) {
			this.universe = universe;
			return this;
		}

		public Builder parameters(Map<RequestParameters,Object> parameters) {
			this.parameters.putAll(parameters);
			return this;
		}

		public Builder parameters(RequestParameters key,Object value) {
			this.parameters.put(key,value);
			return this;
		}

		public Builder parameters(String key,Object value) {
			this.parameters.put(RequestParameters.valueOf(key),value);
			return this;
		}

		public Builder override(Map<RequestOverrides,Object> overrides) {
			this.overrides.putAll(overrides);
			return this;
		}

		public Builder override(RequestOverrides key, Object value) {
			this.overrides.put(key,value);
			return this;
		}

		public Builder identifierType(IdentifierType idType) {
			this.identifierType = idType;
			return this;
		}

		public Builder instrumentType(InstrumentType instrumentType) {
			this.instrumentType = instrumentType;
			return this;
		}
		
		public Builder identifiers(String... identifiers) {
			this.identifiers = identifiers;
			return this;
		}

		public Builder backfill(boolean backfill) {
			this.backfill = backfill;
			return this;
		}

		public Builder subscribe(boolean subscribe) {
			this.subscribe = subscribe;
			return this;
		}

		public Builder dataService(DataServiceEnum dataService) {
			this.dataService = dataService;
			return this;
		}

		public Builder requestType(RequestType requestType) {
			this.requestType = requestType;
			return this;
		}
		
	}
	
}
