package marketdata.services.base;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import config.CoreConfig;
import eventsequencers.Sequenceable;
import exceptions.DataQueryException;
import exceptions.DataServiceStartException;
import finance.identifiers.IIdentifier;
import finance.identifiers.Identifier;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.IPortfolio;
import finance.instruments.InstrumentType;
import marketdata.field.Field;
import marketdata.field.FieldEnum;
import marketdata.services.bloomberg.enumeration.RequestOverrides;
import static marketdata.services.base.RequestParameters.*;

public class DataRequest<K> implements Sequenceable {

	private DataServiceEnum dataService;
	private RequestType requestType;
	private InstrumentType instrumentType;
	private IdentifierType identifierType = CoreConfig.PRIMARY_IDENTIFIER_TYPE;
	private HashSet<IPortfolio> universe;
	private HashSet<IIdentifier> identifiers;
	private ArrayList<Field> fields;
	private Map<RequestParameters,Object> parameters;
	private Map<RequestOverrides,Object> overrides;
	private HashSet<IInstrument> instrumentsQuerySuccess;
	private HashSet<IInstrument> instrumentsQueryError;
	private int instrumentsCompletionCount = 0;
	private boolean completed;
	private boolean backfill = false;
	private boolean subscribe = false;

	public DataRequest() {
		this.universe = new HashSet<>();
		this.identifiers = new HashSet<>();
		this.fields = new ArrayList<>();
		this.parameters = new HashMap<RequestParameters,Object>();
		this.overrides =  new HashMap<RequestOverrides,Object>();
	}

	public DataRequest(Builder<K> builder) {
		this.dataService = builder.dataService;
		this.requestType = builder.requestType;
		this.identifierType = builder.identifierType;
		this.instrumentType = builder.instrumentType;
		this.identifiers = builder.identifiers;
		this.fields = builder.fields;
		this.overrides = builder.overrides;
		this.parameters = builder.parameters;
		this.backfill = builder.backfill;
		this.completed = false;
		this.subscribe = builder.subscribe;
		this.universe = builder.universe;
	}

	public DataServiceEnum getDataService() {
		return dataService;
	}

	public void setDataService(DataServiceEnum dataService) {
		this.dataService = dataService;
	}

	public int getInstrumentsCompletionCount() {
		return instrumentsCompletionCount;
	}

	public void setInstrumentsCompletionCount(int instrumentsCompletionCount) {
		this.instrumentsCompletionCount = instrumentsCompletionCount;
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

	public void setIdentifiers(HashSet<IIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
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

	public HashSet<IInstrument> getInstrumentsQuerySuccess() {
		if(instrumentsQuerySuccess == null)
			instrumentsQuerySuccess = new HashSet<IInstrument>();
		return instrumentsQuerySuccess;
	}

	public void setInstrumentsQuerySuccess(HashSet<IInstrument> instrumentsQuerySuccess) {
		this.instrumentsQuerySuccess = instrumentsQuerySuccess;
	}

	public HashSet<IInstrument> getInstrumentsQueryError() {
		if(instrumentsQueryError == null)
			instrumentsQueryError = new HashSet<IInstrument>();
		return instrumentsQueryError;
	}

	public void setInstrumentsQueryError(HashSet<IInstrument> instrumentsQueryError) {
		this.instrumentsQueryError = instrumentsQueryError;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
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


	public void incrementCompletionCount() {
		this.instrumentsCompletionCount += 1;
	}

	public Set<IIdentifier> getIdentifiers() {
		for(IPortfolio iPortfolio : this.universe) {
			this.identifiers.addAll(iPortfolio.getComposition()
					.stream()
					.map(i -> i.getIdentifier(this.identifierType))
					.collect(Collectors.toSet()));
		}

		if(this.identifiers.isEmpty() && this.instrumentType != null && this.identifierType != null)
			this.identifiers.addAll(CoreConfig.services().instrumentFactory().getIdentifierSet().stream().filter(i -> i.getType().equals(this.identifierType)
					&& i.getInstrument().getInstrumentType().equals(this.instrumentType)).collect(Collectors.toSet()));
		else if(this.identifiers.isEmpty() && this.instrumentType != null)
			this.identifiers.addAll(CoreConfig.services().instrumentFactory().getIdentifierSet().stream().filter(i -> i.getInstrument().getInstrumentType().equals(this.instrumentType)).collect(Collectors.toSet()));
		else if(this.identifiers.isEmpty() && this.identifierType != null)
			this.identifiers.addAll(CoreConfig.services().instrumentFactory().getIdentifierSet().stream().filter(i -> i.getType().equals(this.identifierType)).collect(Collectors.toSet()));
		else if(this.identifiers.isEmpty())
			this.identifiers.addAll(CoreConfig.services().instrumentFactory().getIdentifierSet().stream().collect(Collectors.toSet()));


		return this.identifiers;
	}

	public String[] getSymList() {
		return this.getIdentifiers()
				.stream()
				.map(x -> x.getName())
				.toArray(String[]::new);
	}

	public K query() throws DataQueryException,DataServiceStartException {
		return this.query(this.dataService,this.requestType);
	}

	public void subscribe() throws DataQueryException, DataServiceStartException {
		this.subscribe(this.dataService);
	}

	public K query(DataServiceEnum serviceName, RequestType requestType) throws DataQueryException,DataServiceStartException {
		if(this.subscribe) {
			subscribe(serviceName);
			return null;
		}

		IReferenceDataService<K> service = (IReferenceDataService<K>) CoreConfig.ctx.getBean(serviceName.getReference());
		if(!service.requestIsValid(requestType)) {
			Logger.getRootLogger().log(Level.WARN, serviceName + " service does not accept request " + requestType.name());
			return null;
		}

		if(!service.isOpened())
			service.start();
		K result = service.query(this, requestType);
		if(serviceName.isAsynchronous())
			return queryAsync(requestType); // !!! This blocks the query until completed TODO: Make that optional ?
		else
			return result;
	}

	private K queryAsync(RequestType requestType) throws DataServiceStartException, DataQueryException {
		Logger.getRootLogger().log(Level.INFO, "Waiting for Data Request to complete...");
		synchronized(this) {
			while(!this.isCompleted()) {
				try {
					this.wait(100000); // TODO MOVE TIMEOUT IN CONFIG
				}
				catch(InterruptedException e) {
					throw new DataServiceStartException("Error starting asynchronous query", e);
				}
			}
		}
		Logger.getRootLogger().log(Level.INFO, "Asynchronous Data Request has completed");
		return null;
	}

	public void subscribe(DataServiceEnum serviceName) throws DataQueryException, DataServiceStartException {
		IRealTimeDataService service = (IRealTimeDataService) CoreConfig.ctx.getBean(serviceName.getRealTime());
		if(!service.isOpened())
			service.start();
		service.query(this);
	}

	@Override
	public void execute(Instant t,Object... args) {
		try {
			this.query();
		}
		catch(DataQueryException | DataServiceStartException e) {
			e.printStackTrace();
		}
	}

	public static class Builder<K> {
		private DataServiceEnum dataService;
		private RequestType requestType;
		private IdentifierType identifierType = CoreConfig.PRIMARY_IDENTIFIER_TYPE;
		private InstrumentType instrumentType;
		private HashSet<IPortfolio> universe;
		private HashSet<IIdentifier> identifiers;
		private ArrayList<Field> fields;
		private Map<RequestParameters,Object> parameters;
		private Map<RequestOverrides,Object> overrides;
		private boolean backfill = true;
		private boolean subscribe = false;

		public Builder() {
			universe = new HashSet<IPortfolio>();
			parameters = new HashMap<RequestParameters,Object>();
			overrides = new HashMap<RequestOverrides,Object>();
			identifiers = new HashSet<IIdentifier>();
			fields = new ArrayList<Field>();
			universe = new HashSet<IPortfolio>();
		}

		public Builder<K> universe(String... portfolios) {
			for(String portfolio : portfolios) {
				if(CoreConfig.services().instrumentFactory().hasInstrument(portfolio)) {
					IPortfolio iPortfolio = (IPortfolio) CoreConfig.services().instrumentFactory().getInstrument(portfolio);
					this.universe.add(iPortfolio);
				}
			}
			return this;
		}

		public Builder<K> identifiers(Identifier... identifiers) {
			List<IIdentifier> identifiersList = Arrays.asList(identifiers);
			this.identifiers.addAll(identifiersList);
			return this;
		}

		public Builder<K> identifiers(Collection<? extends Identifier> identifiers) {
			this.identifiers.addAll(identifiers);
			return this;
		}

		public Builder<K> identifiers(IInstrument... instruments) {
			List<IInstrument> instrumentList = Arrays.asList(instruments);
			List<IIdentifier> identifiersList = instrumentList.stream().flatMap(
					x -> x.getIdentifiers().stream().filter(i -> i.getType().equals(identifierType))).collect(Collectors.toList());
			this.identifiers.addAll(identifiersList);
			return this;
		}

		public Builder<K> identifiers(Set<? extends IInstrument> instruments) {
			List<IIdentifier> identifiersList = instruments.stream().flatMap(
					x -> x.getIdentifiers().stream().filter(i -> i.getType().equals(identifierType))).collect(Collectors.toList());
			this.identifiers.addAll(identifiersList);
			return this;
		}

		public Builder<K> identifiers(List<? extends IInstrument> instruments) {
			List<IIdentifier> identifiersList = instruments.stream().flatMap(
					x -> x.getIdentifiers().stream().filter(i -> i.getType().equals(identifierType))).collect(Collectors.toList());
			this.identifiers.addAll(identifiersList);
			return this;
		}

		public Builder<K> identifiers(InstrumentType instrumentType, String... instrumentsNames) {
			identifiers(this.identifierType,instrumentType,instrumentsNames);
			return this;
		}

		public Builder<K> identifiers(InstrumentType instrumentType, Collection<String> instrumentsNames) {
			identifiers(this.identifierType,instrumentType,instrumentsNames);
			return this;
		}

		public Builder<K> identifiers(IdentifierType identifierType, IInstrument[] instrumentsNames) {
			List<IInstrument> instrumentList = Arrays.asList(instrumentsNames);
			List<IIdentifier> identifiersList = instrumentList.stream().flatMap(
					x -> x.getIdentifiers().stream().filter(i -> i.getType().equals(identifierType))).collect(Collectors.toList());
			this.identifiers.addAll(identifiersList);
			return this;
		}

		public Builder<K> identifiers(IdentifierType identifierType, InstrumentType instrumentType,String... instrumentsNames) {
			if(instrumentsNames.length == 0 || instrumentType == null)
				return this;
			Set<IInstrument> instrumentSet = CoreConfig.services().instrumentFactory()
					.makeMultipleInstrument(instrumentType, identifierType, instrumentsNames);
			List<IIdentifier> identifiersList = instrumentSet.stream().flatMap(
					x -> x.getIdentifiers().stream().filter(i -> i.getType().equals(identifierType))).collect(Collectors.toList());

			this.identifiers.addAll(identifiersList);
			return this;
		}

		public Builder<K> identifiers(IdentifierType identifierType, InstrumentType instrumentType,Collection<String> instrumentsNames) {
			Set<IInstrument> instruments = CoreConfig.services().instrumentFactory()
					.makeMultipleInstrument(instrumentType, identifierType, instrumentsNames);
			List<IIdentifier> identifiersList = instruments.stream().flatMap(
					x -> x.getIdentifiers().stream().filter(i -> i.getType().equals(identifierType))).collect(Collectors.toList());

			this.identifiers.addAll(identifiersList);
			return this;
		}

		public Builder<K> fields(FieldEnum... fields) {
			List<Field> fieldList = Arrays.asList(fields);
			this.fields.addAll(fieldList);
			return this;
		}

		public Builder<K> fields(Field... fields) {
			List<Field> fieldList = Arrays.asList(fields);
			this.fields.addAll(fieldList);
			return this;
		}

		public Builder<K> fields(String... fields) {
			if(fields == null)
				return this;
			Set<String> fieldStringSet = new LinkedHashSet<String>(Arrays.asList(fields));
			List<Field> fieldList = fieldStringSet.stream().map(x -> Field.get(x)).collect(Collectors.toList());
			this.fields.addAll(fieldList);
			return this;
		}

		public Builder<K> fields(Collection<? extends Field> fields) {
			this.fields.addAll(fields);
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

		public Builder<K> intradaySessionLondon(LocalDate localDate) {
			ZonedDateTime sodLocal = localDate.atStartOfDay(ZoneId.of("Europe/London"));
			ZonedDateTime tradingStart;
			tradingStart = sodLocal
					.withHour(8)
					.withMinute(0)
					.withSecond(0);
			ZonedDateTime tradingEnd;
			tradingEnd = sodLocal
					.withHour(16)
					.withMinute(35)
					.withSecond(0);
			parameters(startDateTime,tradingStart);
			parameters(endDateTime,tradingEnd);
			return this;
		}

		public DataRequest<K> build() {
			return new DataRequest<K>(this);
		}
	}

}


