package marketdata.services.bloomberg.responsehandler;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Session;

import config.CoreConfig;
import eventprocessors.EventPriorityQueue;
import events.MarketDataEventFactory;
import events.PortfolioCompositionEvent;
import finance.identifiers.IdentifierType;
import finance.instruments.IInstrument;
import finance.instruments.IPortfolio;
import finance.instruments.Instrument;
import finance.instruments.InstrumentFactory;
import finance.instruments.InstrumentType;
import marketdata.container.MarketDataContainerEnum;
import marketdata.field.Field;
import marketdata.services.base.AbstractResponseHandler;
import marketdata.services.base.DataRequest;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.bloomberg.enumeration.RequestOverrides;
import marketdata.services.bloomberg.enumeration.ResponseType;
import marketdata.services.bloomberg.services.BBGReferenceDataService;

@Service
@Scope("singleton")
@Lazy(true)
public class BBGReferenceResponseHandler extends AbstractResponseHandler<BBGReferenceDataService> implements EventHandler{
	@Autowired
	InstrumentFactory factory;
	@Autowired
	private MarketDataEventFactory marketDataEventFactory;
	@Autowired
	private EventPriorityQueue queue;

	public BBGReferenceResponseHandler() {

	}

	@Override
	@Autowired
	public void setDataService(BBGReferenceDataService dataService) {
		this.dataService = dataService;
	}

	@Override
	public void processEvent(Event event, Session session) {
		switch (event.eventType().intValue()) {
		case Event.EventType.Constants.SESSION_STATUS: {
			MessageIterator iter = event.messageIterator();
			while (iter.hasNext()) {
				Message message = iter.next();
				if (message.messageType().equals("SessionStarted")) {
					try {
						session.openServiceAsync(this.getDataService().getServiceType().getSyntax(),new CorrelationID(99));
					} catch (Exception e) {
						System.err.println("Could not open //blp/refdata for async");
					}
				} 
			}
			break;
		}
		case Event.EventType.Constants.SERVICE_STATUS: {
			MessageIterator iter = event.messageIterator();
			while (iter.hasNext()) {
				Message message = iter.next();
				if (message.correlationID().value() == 99 && message.messageType().equals("ServiceOpened")) {
					this.getDataService().setOpened(true);
					this.getDataService().notifyAll();
				} else {
					System.out.println("Unexpected SERVICE_STATUS message:");
					try {
						message.print(System.err);
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
			break;
		}
		case Event.EventType.Constants.PARTIAL_RESPONSE: {//
			try {
				processResponse(event,false);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			break;
		}
		case Event.EventType.Constants.RESPONSE:{
			try {
				processResponse(event,true);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
			break;
		}
		default: {
			break;
		}
		}
	}

	private void processResponse(Event event,boolean isFinal) throws Exception {

		MessageIterator messageIterator = event.messageIterator();
		DataRequest<Object> request = null;

		while (messageIterator.hasNext()){
			Message message = messageIterator.next();
			Name messageType = message.messageType();
			CorrelationID correlationID = message.correlationID();
			ResponseType responseType = ResponseType.valueOf(messageType.toString());
			request = this.dataService.getRequestBuilderMap().get(correlationID);
			Request bbgrequest = this.dataService.getRequestMap().get(correlationID);
			boolean responseError = message.asElement().hasElement("responseError");
			if(responseError) {
				onResponseError(bbgrequest,responseType,request);
				continue;
			}

			switch(responseType) {
			case ReferenceDataResponse:
				processReferenceDataResponse(message,request);
				break;
			case HistoricalDataResponse:
				processHistoricalDataResponse(message,request);
				break;
			case IntradayTickResponse:
				processIntradayTickResponse(message,request,bbgrequest);
				break;
			case IntradayBarResponse:
				processIntradayBarResponse(message,request,bbgrequest);
				break;
			}

			try {
				message.print(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(request.getInstrumentsCompletionCount() >= request.getIdentifiers().size())
			terminateRequest(request);
	}

	public synchronized void terminateRequest(DataRequest<Object> request) {
		synchronized(request) {
			request.setCompleted(true);
			request.notifyAll();
		}
	}

	private void onResponseError(Request bbgrequest, ResponseType responseType, DataRequest<Object> request) {
		Element security = bbgrequest.getElement("security");
		IInstrument instrument = this.getDataService().getInstrumentFromBloombergResponse(security.getValueAsString());
		request.getInstrumentsQueryError().add(instrument);
		request.incrementCompletionCount();
	}


	private void processReferenceDataResponse(Message msg,DataRequest<Object> request) throws Exception
	{
		Element securityDataArray = msg.getElement("securityData");
		for (int i = 0; i < securityDataArray.numValues(); ++i) {
			Element securityData = securityDataArray.getValueAsElement(i);
			String security = securityData.getElementAsString("security");		
			IInstrument instrument = this.getDataService().getInstrumentFromBloombergResponse(security);
			if (!securityData.hasElement("securityError")) {
				Element fieldData = securityData.getElement("fieldData");
				for (int j = 0; j < fieldData.numElements(); ++j) {
					Element item = fieldData.getElement(j);
					Instant timestamp = ZonedDateTime.now().toInstant();
					if(request.getOverrides().containsKey(RequestOverrides.END_DATE_OVERRIDE)) { // TODO check other use of datetime overrides such as historical vwap
						LocalDate ldOverride = (LocalDate) request.getOverrides().get(RequestOverrides.END_DATE_OVERRIDE);
						timestamp = ldOverride.atStartOfDay(CoreConfig.GLOBAL_ZONE_ID).toInstant();
					}
					
					Field field = request.getFields().get(j); // TODO: cant do this if there is a fieldExceptions as ordering is lost.
					
					if(item.numValues() > 1) { // We're dealing with an array, equivalent to a BDS call
						processReferenceDataResponseArray(timestamp,item,request,instrument,field);
					}
					else {
						Object value = field.getValueFromBloombergElement(item);
						
						marketDataEventFactory.publishToEventQueue(
								request.isBackfill(), 
								timestamp, 
								MarketDataContainerEnum.SPOT, 
								DataServiceEnum.BLOOMBERG, 
								timestamp,  
								timestamp,  
								instrument, 
								field, 
								value);					
					}
				}
			}
			else {
				request.getInstrumentsQueryError().add(instrument);
			}
			request.incrementCompletionCount();
		}
	}
	
	private void processReferenceDataResponseArray(Instant timestamp,Element item,DataRequest<Object> request,IInstrument instrument,Field field) {
		String itemName = item.name().toString();
		for (int k = 0; k < item.numValues(); ++k) {
			Element subitem = item.getValueAsElement(k);				
			switch(itemName) {
				case "INDX_MWEIGHT_HIST": 
					String security = subitem.getElementAsString("Index Member");
					IInstrument member = factory.makeInstrument(InstrumentType.SingleStock, IdentifierType.TICKER, security); // TODO: Member could be another asset type or a cash component
					Double weight = subitem.getElementAsFloat64("Percent Weight");
					IPortfolio portfolio = (IPortfolio) instrument;					
					PortfolioCompositionEvent event = new PortfolioCompositionEvent(timestamp, portfolio, member,weight);	
					queue.add(event);
					break;
			}
			
		}
	}

	private void processHistoricalDataResponse(Message msg,DataRequest<Object> request) throws Exception {
		Element securityData = msg.getElement("securityData");
		String security = securityData.getElementAsString("security");		
		IInstrument instrument = this.getDataService().getInstrumentFromBloombergResponse(security);
		if (!securityData.hasElement("securityError")) {
			request.getInstrumentsQuerySuccess().add(instrument);
			Element fieldDataArray = securityData.getElement("fieldData");
			for (int j = 0; j < fieldDataArray.numValues(); ++j) {
				Element fieldData = fieldDataArray.getValueAsElement(j);
				Element dateField = fieldData.getElement(0);
				Datetime date = dateField.getValueAsDate();								
				LocalDate ld = this.getDataService().convertBbgToJavaLocalDate(date);

				for (int k = 1; k < fieldData.numElements(); ++k) {
					Element fieldElement = fieldData.getElement(k);
					Field field = request.getFields().get(k-1);
					Instant timestamp = field.assignTimeStamp(ld,((Instrument) instrument).getExchange()).toInstant();
					Object value = field.getValueFromBloombergElement(fieldElement);

					marketDataEventFactory.publishToEventQueue(
							request.isBackfill(), 
							timestamp, 
							MarketDataContainerEnum.DAY, 
							DataServiceEnum.BLOOMBERG, 
							timestamp,  
							timestamp,  
							instrument, 
							field, 
							value);
				}
			}
		}
		else {
			request.getInstrumentsQueryError().add(instrument);
		}
		request.incrementCompletionCount();
	}

	private void processIntradayTickResponse(Message msg,DataRequest<Object> request,Request bbgrequest) throws Exception
	{
		Element security = bbgrequest.getElement("security");
		IInstrument instrument = this.dataService.getInstrumentFromBloombergResponse(security.getValueAsString());
		Element data = msg.getElement("tickData").getElement("tickData");
		int numItems = data.numValues();

		for (int i = 0; i < numItems; ++i) {
			Element item = data.getValueAsElement(i);
			Datetime bbgtimestamp = item.getElementAsDate("time");
			String type = item.getElementAsString("type");
			int size = item.getElementAsInt32("size");
			String cc;
			if (item.hasElement("conditionCodes")) {
				cc = item.getElementAsString("conditionCodes");
			}

			Field field = Field.get(type);
			Object value = field.getValueFromBloombergElement(item);

			Instant timestamp = this.dataService.convertBbgToJavaInstant(bbgtimestamp);
			marketDataEventFactory.publishToEventQueue(
					request.isBackfill(), 
					timestamp, 
					MarketDataContainerEnum.TICK, 
					DataServiceEnum.BLOOMBERG, 
					timestamp,  
					timestamp,  
					instrument, 
					field, 
					value);
		}
		request.incrementCompletionCount();
	}

	private void processIntradayBarResponse(Message msg,DataRequest<Object> request,Request bbgrequest) throws Exception {
		Element security = bbgrequest.getElement("security");
		IInstrument instrument = this.dataService.getInstrumentFromBloombergResponse(security.getValueAsString());
		Element data = msg.getElement("barData").getElement("barTickData");
		int numBars = data.numValues();

		for (int i = 0; i < numBars; ++i) {
			Element bar = data.getValueAsElement(i);
			Datetime bbgtimestamp = bar.getElementAsDate("time");
			Instant timestamp = this.dataService.convertBbgToJavaInstant(bbgtimestamp);

			for(Field field : request.getFields()) {					
				Element item;
				switch(field.name()) {
				case "PX_LAST":
					item = bar.getElement("close");
					break;
				case "OPEN":
					item = bar.getElement("open");
					break;
				case "HIGH":
					item = bar.getElement("high");
					break;
				case "LOW":
					item = bar.getElement("low");
					break;	
				case "VOLUME":
					item = bar.getElement("volume");
					break;	//TODO add number of events as a field
				default:
					continue;
				}

				Object value = field.getValueFromBloombergElement(item);

				marketDataEventFactory.publishToEventQueue(
						request.isBackfill(), 
						timestamp, 
						MarketDataContainerEnum.BAR, 
						DataServiceEnum.BLOOMBERG, 
						timestamp,  
						timestamp,  
						instrument, 
						field, 
						value);
			}

		}
		request.incrementCompletionCount();
	}


}
