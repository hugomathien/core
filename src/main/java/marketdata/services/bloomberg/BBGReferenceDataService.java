package marketdata.services.bloomberg;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Session;

import exceptions.DataQueryException;
import finance.identifiers.IBloombergIdentifier;
import finance.identifiers.Identifier;
import marketdata.field.Field;
import marketdata.services.base.DataRequest;
import marketdata.services.base.IReferenceDataService;
import marketdata.services.base.RequestParameters;
import marketdata.services.base.RequestType;
import marketdata.services.bloomberg.utils.RequestOverrides;
import marketdata.services.bloomberg.utils.ServiceType;

@Service("BLOOMBERG_REFERENCE")
@Scope("singleton")
@Lazy(true)
public class BBGReferenceDataService extends BBGService implements IReferenceDataService {

	private HashMap<CorrelationID, Request> requestMap;
	private HashMap<CorrelationID, DataRequest> dataRequestMap;
	@Autowired
	private BBGReferenceResponseHandler responseHandler;

	public BBGReferenceDataService() {
		super(ServiceType.REFERENCE_DATA);
	}

	@PostConstruct
	public void init() {
		this.requestMap = new HashMap<CorrelationID, Request>();
		this.dataRequestMap = new HashMap<CorrelationID, DataRequest>();
		Session session = new Session(this.getSessionOptions(), responseHandler);
		this.setSession(session);
	}

	public HashMap<CorrelationID, Request> getRequestMap() {
		return requestMap;
	}

	public void setRequestMap(HashMap<CorrelationID,Request> requestMap) {
		this.requestMap = requestMap;
	}

	public HashMap<CorrelationID, DataRequest> getDataRequestMap() {
		return dataRequestMap;
	}

	public void setDataRequestMap(HashMap<CorrelationID, DataRequest> dataRequestMap) {
		this.dataRequestMap = dataRequestMap;
	}

	public void query(DataRequest dataRequest)  throws DataQueryException {
		try {
			switch(dataRequest.getRequestType()) {
			case ReferenceDataRequest:
				this.referenceDataRequest(dataRequest);
				break;
			case HistoricalDataRequest:
				this.historicalDataRequest(dataRequest);
				break;
			case IntradayTickRequest:
				this.intradayTickRequest(dataRequest);
				break;
			case IntradayBarRequest:
				this.intradayBarRequest(dataRequest);
				break;
			case UniverseRequest:
				this.universeRequest(dataRequest);
				break;
			default:
				break;
			}
		} catch (DataQueryException e) {
			e.printStackTrace();
		}
	}

	private synchronized void sendRequest(Request request,DataRequest dataRequest) throws IOException {
		int newId = requestMap.size() + 1;
		CorrelationID correlationID = new CorrelationID(newId);
		this.getRequestMap().put(correlationID, request);
		this.dataRequestMap.put(correlationID, dataRequest);
		this.getSession().sendRequest(request,correlationID);
	}

	private void universeRequest(DataRequest dataRequest) throws DataQueryException {
		dataRequest.setFields(new ArrayList<Field>());
		dataRequest.getFields().add(Field.get("INDX_MWEIGHT_HIST"));
		if(dataRequest.getParameters().containsKey(RequestParameters.startDate) && !dataRequest.getOverrides().containsKey(RequestOverrides.START_DATE_OVERRIDE))
			dataRequest.getOverrides().put(RequestOverrides.START_DATE_OVERRIDE,dataRequest.getParameters().get(RequestParameters.startDate));
		if(dataRequest.getParameters().containsKey(RequestParameters.endDate) && !dataRequest.getOverrides().containsKey(RequestOverrides.END_DATE_OVERRIDE))
			dataRequest.getOverrides().put(RequestOverrides.END_DATE_OVERRIDE,dataRequest.getParameters().get(RequestParameters.endDate));
		dataRequest.getParameters().remove(RequestParameters.startDate);
		dataRequest.getParameters().remove(RequestParameters.endDate);
		this.referenceDataRequest(dataRequest);
	}

	private void referenceDataRequest(DataRequest dataRequest) throws DataQueryException {
		Request request = this.getService().createRequest("ReferenceDataRequest");
		request = this.appendInstruments(dataRequest, request);
		request = this.appendFields(dataRequest, request);
		request = this.setParameters(dataRequest, request);
		request = this.setOverrides(dataRequest, request);
		try {
			sendRequest(request,dataRequest);
		} catch(IOException e) {
			throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
		}
	}

	private void historicalDataRequest(DataRequest dataRequest) throws DataQueryException {
		Request request = this.getService().createRequest("HistoricalDataRequest");
		request = this.appendInstruments(dataRequest, request);
		request = this.appendFields(dataRequest, request);
		request = this.setParameters(dataRequest, request);
		request = this.setOverrides(dataRequest, request);
		try {
			sendRequest(request,dataRequest);
		} catch(IOException e) {
			throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
		}
	}

	private void intradayTickRequest(DataRequest dataRequest) throws DataQueryException {
		for(Identifier security : dataRequest.getIdentifiers()) {
			Request request = this.getService().createRequest("IntradayTickRequest");
			request = this.setSecurity(request, security);
			request = this.appendEventTypes(dataRequest, request);
			request = this.setParameters(dataRequest, request);
			try {
				sendRequest(request,dataRequest);
			} catch(IOException e) {
				throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
			}
		}
	}
	
	private void intradayBarRequest(DataRequest dataRequest) throws DataQueryException {
		for(Identifier security : dataRequest.getIdentifiers()) {
			for(Field field : dataRequest.getFields()) {
				Request request = this.getService().createRequest("IntradayBarRequest");
				request = this.setSecurity(request,security);
				request = this.setEventType(request, field);
				request = this.setParameters(dataRequest, request);
				try {
					sendRequest(request,dataRequest);
				} catch(IOException e) {
					throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
				}
			}
		}
	}
	
	private Request appendInstruments(DataRequest dataRequest, Request request) {
		for(Identifier security : dataRequest.getIdentifiers()) {
			String bbgSyntax = ((IBloombergIdentifier) security).getBbgQuerySyntax();
			request.append("securities", bbgSyntax);
		}
		return request;
	}
	
	private Request appendFields(DataRequest dataRequest, Request request) {
		for(Field field : dataRequest.getFields()) {
			request.append("fields", field.name(this.getServiceName()));
		}
		return request;
	}
	
	private Request appendEventTypes(DataRequest dataRequest, Request request) {
		for(Field field : dataRequest.getFields()) {
			request.append("eventTypes", field.name(this.getServiceName()));
		}
		return request;
	}
	
	private Request setSecurity(Request request, Identifier security) {
		String bbgSyntax = ((IBloombergIdentifier) security).getBbgQuerySyntax();
		request.set("security", bbgSyntax);
		return request;
	}
	
	private Request setEventType(Request request, Field field) {
		request.set("eventType", field.name(this.getServiceName()));
		return request;
	}
	
	private Request setParameters(DataRequest dataRequest, Request request) {
		for(Entry<RequestParameters, Object> entry : dataRequest.getParameters().entrySet()) {
			RequestParameters parameter = entry.getKey();
			Object value = entry.getValue();
			
			if(value instanceof Integer) {
				request.set(parameter.name(),(Integer) value);
			}
			else if(value instanceof Double) {
				request.set(parameter.name(), (Double) value);
			}
			else if(value instanceof String) {
				request.set(parameter.name(), (String) value);
			}
			else if(value instanceof ZonedDateTime) {
				String bbgdt = this.convertJavaZonedDateTimeToBBGString((ZonedDateTime) value);
				request.set(parameter.name(), bbgdt);
			}
			else if (value instanceof LocalDate) {
				String bbgdt = BBGService.convertJavaLocalDateToBBGString((LocalDate) value);
				request.set(parameter.name(), bbgdt);
			}
			else if(value instanceof Boolean) {
				request.set(parameter.name(), (boolean) value);
			}
		}
		
		return request;
	}
	
	private Request setOverrides(DataRequest dataRequest, Request request) {
		Element overrides = request.getElement("overrides");
		if(dataRequest.getOverrides() == null)
			return request;
		
		for(Entry<RequestOverrides, Object> entry : dataRequest.getOverrides().entrySet()) {
			RequestOverrides override = entry.getKey();
			Element overrideElement = overrides.appendElement();
			overrideElement.setElement("fieldId", override.name());
			
			Object value = entry.getValue();
			
			if(value instanceof Integer)
				overrideElement.setElement("value", (Integer) value);
			else if(value instanceof Double)
				overrideElement.setElement("value", (Double) value);
			else if(value instanceof String)
				overrideElement.setElement("value", (String) value);
			else if(value instanceof LocalDate) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				overrideElement.setElement("value", ((LocalDate) value).format(formatter));
			}
		}
		
		return request;
	}
	
	public BBGReferenceResponseHandler getResponseHandler() {
		return responseHandler;
	}

	@Override
	@Value("${bloomberg.requestTypes}")
	public void setRequestDiscovery(RequestType[] requestDiscovery) {
		this.requestDiscovery = requestDiscovery;
	}

}
