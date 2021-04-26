package marketdata.services.bloomberg;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import marketdata.services.bloomberg.enumeration.RequestOverrides;
import marketdata.services.bloomberg.enumeration.ServiceType;
import marketdata.services.bloomberg.responsehandler.BBGReferenceResponseHandler;

@Service("BLOOMBERG_REFERENCE")
@Scope("singleton")
@Lazy(true)
public class BBGReferenceDataService extends BBGService implements IReferenceDataService<Object> {

	private HashMap<CorrelationID, Request> requestMap;
	private HashMap<CorrelationID, DataRequest<Object>> requestBuilderMap;
	@Autowired
	private BBGReferenceResponseHandler responseHandler;

	public BBGReferenceDataService() {
		super(ServiceType.REFERENCE_DATA);
	}

	@PostConstruct
	public void init() {
		this.requestMap = new HashMap<CorrelationID, Request>();
		this.requestBuilderMap = new HashMap<CorrelationID, DataRequest<Object>>();
		Session session = new Session(this.getSessionOptions(), responseHandler);
		this.setSession(session);
	}

	public HashMap<CorrelationID, Request> getRequestMap() {
		return requestMap;
	}

	public void setRequestMap(HashMap<CorrelationID,Request> requestMap) {
		this.requestMap = requestMap;
	}

	public HashMap<CorrelationID, DataRequest<Object>> getRequestBuilderMap() {
		return requestBuilderMap;
	}

	public void setRequestBuilderMap(HashMap<CorrelationID, DataRequest<Object>> requestBuilderMap) {
		this.requestBuilderMap = requestBuilderMap;
	}

	public Object query(DataRequest<Object> requestBuilder, RequestType requestType)  throws DataQueryException {
		try {
			switch(requestType) {
			case ReferenceDataRequest:
				this.referenceDataRequest(requestBuilder);
				break;
			case HistoricalDataRequest:
				this.historicalDataRequest(requestBuilder);
				break;
			case IntradayTickRequest:
				this.intradayTickRequest(requestBuilder);
				break;
			case IntradayBarRequest:
				this.intradayBarRequest(requestBuilder);
				break;
			default:
				break;
			}
		} catch (DataQueryException e) {
			e.printStackTrace();
		}
		return null;
	}

	private synchronized void sendRequest(Request request,DataRequest<Object> requestBuilder) throws IOException {
		int newId = requestMap.size() + 1;
		CorrelationID correlationID = new CorrelationID(newId);
		this.getRequestMap().put(correlationID, request);
		this.getRequestBuilderMap().put(correlationID, requestBuilder);
		this.getSession().sendRequest(request,correlationID);
	}

	private void referenceDataRequest(DataRequest<Object> requestBuilder) throws DataQueryException {
		Request request = this.getService().createRequest("ReferenceDataRequest");
		request = this.appendInstruments(requestBuilder, request);
		request = this.appendFields(requestBuilder, request);
		request = this.setParameters(requestBuilder, request);
		request = this.setOverrides(requestBuilder, request);
		try {
			sendRequest(request,requestBuilder);
		} catch(IOException e) {
			throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
		}
	}

	private void historicalDataRequest(DataRequest<Object> requestBuilder) throws DataQueryException {
		Request request = this.getService().createRequest("HistoricalDataRequest");
		request = this.appendInstruments(requestBuilder, request);
		request = this.appendFields(requestBuilder, request);
		request = this.setParameters(requestBuilder, request);
		request = this.setOverrides(requestBuilder, request);
		try {
			sendRequest(request,requestBuilder);
		} catch(IOException e) {
			throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
		}
	}

	private void intradayTickRequest(DataRequest<Object> requestBuilder) throws DataQueryException {
		for(Identifier security : requestBuilder.getIdentifiers()) {
			Request request = this.getService().createRequest("IntradayTickRequest");
			request = this.setSecurity(request, security);
			request = this.appendEventTypes(requestBuilder, request);
			request = this.setParameters(requestBuilder, request);
			try {
				sendRequest(request,requestBuilder);
			} catch(IOException e) {
				throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
			}
		}
	}
	
	private void intradayBarRequest(DataRequest<Object> requestBuilder) throws DataQueryException {
		for(Identifier security : requestBuilder.getIdentifiers()) {
			for(Field field : requestBuilder.getFields()) {
				Request request = this.getService().createRequest("IntradayBarRequest");
				request = this.setSecurity(request,security);
				request = this.setEventType(request, field);
				request = this.setParameters(requestBuilder, request);
				try {
					sendRequest(request,requestBuilder);
				} catch(IOException e) {
					throw new DataQueryException("Bloomberg Reference Data Service query failed" + request.toString(),e);
				}
			}
		}
	}
	
	private Request appendInstruments(DataRequest<Object> requestBuilder, Request request) {
		for(Identifier security : requestBuilder.getIdentifiers()) {
			String bbgSyntax = ((IBloombergIdentifier) security).getBbgQuerySyntax();
			request.append("securities", bbgSyntax);
		}
		return request;
	}
	
	private Request appendFields(DataRequest<Object> requestBuilder, Request request) {
		for(Field field : requestBuilder.getFields()) {
			request.append("fields", field.name(this.getServiceName()));
		}
		return request;
	}
	
	private Request appendEventTypes(DataRequest<Object> requestBuilder, Request request) {
		for(Field field : requestBuilder.getFields()) {
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
	
	private Request setParameters(DataRequest<Object> requestBuilder, Request request) {
		for(Entry<RequestParameters, Object> entry : requestBuilder.getParameters().entrySet()) {
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
	
	private Request setOverrides(DataRequest<Object> requestBuilder, Request request) {
		Element overrides = request.getElement("overrides");
		if(requestBuilder.getOverrides() == null)
			return request;
		
		for(Entry<RequestOverrides, Object> entry : requestBuilder.getOverrides().entrySet()) {
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
