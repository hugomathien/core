package marketdata.services.bloomberg;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
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
import event.events.MarketDataEventFactory;
import event.events.PortfolioCompositionEvent;
import event.processing.EventPriorityQueue;
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
import marketdata.services.bloomberg.utils.RequestOverrides;
import marketdata.services.bloomberg.utils.ResponseType;

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
	private long msgCount = 0;
	private Queue<Event> bloombergEventQueue;

	public BBGReferenceResponseHandler() {
		this.bloombergEventQueue = new LinkedList<Event>();
	}

	@Override
	@Autowired
	public void setDataService(BBGReferenceDataService dataService) {
		this.dataService = dataService;
	}

	private void processEventQueue() {

		while(!this.bloombergEventQueue.isEmpty()) {
			Event event = this.bloombergEventQueue.poll();
			switch (event.eventType().intValue()) {
				case Event.EventType.Constants.PARTIAL_RESPONSE: {
					try {
						processResponse(event, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
				case Event.EventType.Constants.RESPONSE: {
					try {
						processResponse(event, true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}

	@Override
	public void processEvent(Event event, Session session) {
		this.msgCount += 1;
		CoreConfig.logger.log(Level.INFO,"BBG Reference Response msg received="+event.eventType().intValue()+ " msg"+this.msgCount);
		switch (event.eventType().intValue()) {
			case Event.EventType.Constants.SESSION_STATUS: {
				MessageIterator iter = event.messageIterator();
				while (iter.hasNext()) {
					Message message = iter.next();
					if (message.messageType().equals("SessionStarted")) {
						try {
							session.openServiceAsync(this.getDataService().getServiceType().getSyntax(),new CorrelationID(99));
						} catch (Exception e) {
							CoreConfig.logger.log(Level.ERROR, "Bloomberg Reference Response: Could not open //blp/refdata for async");
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
						CoreConfig.logger.log(Level.ERROR, "Bloomberg Reference Response: Unexpected SERVICE_STATUS message");
						try {
							message.print(System.err);
						} catch (Exception e){
							e.printStackTrace();
						}
					}
				}
				break;
			}
			case Event.EventType.Constants.PARTIAL_RESPONSE: {
				try {
					this.bloombergEventQueue.add(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case Event.EventType.Constants.RESPONSE:{
				try {
					this.bloombergEventQueue.add(event);
					this.processEventQueue();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				break;
			}
			default: {
				System.out.println("other");
				break;
			}

		}
	}


	private synchronized void processResponse(Event event,boolean isFinal) throws Exception {
		/** TODO: handle daily capacity reach
		 *
		 * RESPONSE = {
		 *     {
		 *         CID: { User: 1 }
		 *         RequestId=324f0b3b-16aa-4298-8111-0ad226285d19
		 *         HistoricalDataResponse = {
		 *             responseError = {
		 *                 source = "rsfhdsvc2"
		 *                 code = -4001
		 *                 category = "LIMIT"
		 *                 message = "Daily capacity reached. [nid:53245] "
		 *                 subcategory = "DAILY_CAPACITY_REACHED"
		 *             }
		 *         }
		 *     }
		 * }
		 */

		MessageIterator messageIterator = event.messageIterator();
		DataRequest request = null;

		while (messageIterator.hasNext()){
			Message message = messageIterator.next();
			Name messageType = message.messageType();
			CorrelationID correlationID = message.correlationID();
			ResponseType responseType = ResponseType.valueOf(messageType.toString());
			request = this.dataService.getDataRequestMap().get(correlationID);
			Request bbgRequest = this.dataService.getRequestMap().get(correlationID);
			boolean responseError = message.asElement().hasElement("responseError");
			if(responseError) {
				onResponseError(bbgRequest,responseType,request);
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
					processIntradayTickResponse(message,request,bbgRequest);
					break;
				case IntradayBarResponse:
					processIntradayBarResponse(message,request,bbgRequest);
					break;
			}
		}

		if(request.getInstrumentsCompletionCount() >= request.getIdentifiers().size())
			terminateRequest(request);
	}

	public synchronized void terminateRequest(DataRequest request) {
		synchronized(request) {
			request.setCompleted(true);
			request.notifyAll();
		}
	}

	private void onResponseError(Request bbgrequest, ResponseType responseType, DataRequest request) {
		if(bbgrequest.hasElement("security")) {
			Element security = bbgrequest.getElement("security");
			IInstrument instrument = this.getDataService().getInstrumentFromBloombergResponse(security.getValueAsString());
			request.getInstrumentsQueryError().add(instrument);
		}
		request.incrementCompletionCount();
	}


	private void processReferenceDataResponse(Message msg,DataRequest request) throws Exception
	{
		Element securityDataArray = msg.getElement("securityData");
		for (int i = 0; i < securityDataArray.numValues(); ++i) {
			Element securityData = securityDataArray.getValueAsElement(i);
			String security = securityData.getElementAsString("security");
			IInstrument instrument = this.getDataService().getInstrumentFromBloombergResponse(security);
			if (!securityData.hasElement("securityError")) {

				List<Field> validFields = this.filterFieldsForExceptions(securityData, request.getFields());

				Element fieldData = securityData.getElement("fieldData");

				for (int j = 0; j < fieldData.numElements(); ++j) {
					Element item = fieldData.getElement(j);
					Instant timestamp = ZonedDateTime.now().toInstant();
					if(request.getOverrides().containsKey(RequestOverrides.END_DATE_OVERRIDE)) { // TODO check other use of datetime overrides such as historical vwap
						LocalDate ldOverride = (LocalDate) request.getOverrides().get(RequestOverrides.END_DATE_OVERRIDE);
						timestamp = ldOverride.atStartOfDay(CoreConfig.GLOBAL_ZONE_ID).toInstant();
					}

					try {
						Field field = validFields.get(j);
						Optional<Field> requestfield = validFields.stream().filter(f -> f.name(DataServiceEnum.BLOOMBERG).equals(item.name().toString())).findFirst();
						if (requestfield.isPresent()) // usually because of an override the query uses a custom field that does not have a bloomberg match.
							field = requestfield.get();


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
					catch(Exception e) {
						System.out.println(e.toString());
					}
				}
			}
			else {
				request.getInstrumentsQueryError().add(instrument);
			}
			request.incrementCompletionCount(); // TODO: Log progress
		}
	}

	private void processReferenceDataResponseArray(Instant timestamp,Element item,DataRequest request,IInstrument instrument,Field field) {
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
					// TODO: When we have finished processing the portfolio composition, create a terminal event to identify the additions/ and generate entry/exit portfolio events
					break;
			}
		}
	}

	private void processHistoricalDataResponse(Message msg,DataRequest request) throws Exception {
		Element securityData = msg.getElement("securityData");
		String security = securityData.getElementAsString("security");
		IInstrument instrument = this.getDataService().getInstrumentFromBloombergResponse(security);

		if (!securityData.hasElement("securityError")) {
			request.getInstrumentsQuerySuccess().add(instrument);
			Element fieldDataArray = securityData.getElement("fieldData");
			List<Field> validFields = this.filterFieldsForExceptions(securityData, request.getFields());

			for (int j = 0; j < fieldDataArray.numValues(); ++j) {
				Element fieldData = fieldDataArray.getValueAsElement(j);
				Element dateField = fieldData.getElement(0);
				Datetime date = dateField.getValueAsDate();
				LocalDate ld = this.getDataService().convertBbgToJavaLocalDate(date);

				for (int k = 1; k < fieldData.numElements(); ++k) {
					Element fieldElement = fieldData.getElement(k);
					Field field = validFields.get(k-1);

					Optional<Field> requestfield = validFields.stream().filter(f -> f.name(DataServiceEnum.BLOOMBERG).equals(fieldElement.name().toString())).findFirst();
					if(requestfield.isPresent()) // when false the query uses a custom field that does not have a bloomberg match (happens when using overrides).
						field = requestfield.get();

					Instant timestamp;
					try {
						timestamp = field.assignTimeStamp(ld,((Instrument) instrument).getExchange()).toInstant();
					}
					catch(Exception e) {
						timestamp = ld.atStartOfDay(CoreConfig.GLOBAL_ZONE_ID).toInstant();
					}

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

	private void processIntradayTickResponse(Message msg,DataRequest request,Request bbgrequest) throws Exception
	{
		Element security = bbgrequest.getElement("security");
		IInstrument instrument = this.dataService.getInstrumentFromBloombergResponse(security.getValueAsString());
		Element data = msg.getElement("tickData").getElement("tickData");
		int numItems = data.numValues();

		for (int i = 0; i < numItems; ++i) {
			Element item = data.getValueAsElement(i);
			Element elval = item.getElement("value");
			Datetime bbgtimestamp = item.getElementAsDate("time");
			String type = item.getElementAsString("type");
			int size = item.getElementAsInt32("size");
			String cc;
			if (item.hasElement("conditionCodes")) {
				cc = item.getElementAsString("conditionCodes");
			}

			Field field = Field.get(type);
			Object value = field.getValueFromBloombergElement(elval);

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

	private void processIntradayBarResponse(Message msg,DataRequest request,Request bbgrequest) throws Exception {
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
					case "TRADE":
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

	@Deprecated // This method does not need to be called if we are mapping the field from the bloomberg response to the query (rather than using ordering)
	private List<Field> filterFieldsForExceptions(Element securityData,List<Field> fieldList) {
		if(!securityData.hasElement("fieldExceptions"))
			return fieldList;
		Element fieldExceptions = securityData.getElement("fieldExceptions");
		List<Field> filteredList = new ArrayList<Field>();
		filteredList.addAll(fieldList);
		Stream<Field> fieldStream = filteredList.stream();

		for (int j = 0; j < fieldExceptions.numValues(); ++j) {
			Element fieldException = fieldExceptions.getValueAsElement(j);
			String fieldExceptedStr = fieldException.getElement("fieldId").getValueAsString();
			Optional<Field> fieldExcepted = fieldStream.filter(f -> f.name(DataServiceEnum.BLOOMBERG).equals(fieldExceptedStr)).findFirst();
			if(fieldExcepted.isPresent())
				filteredList.remove(fieldExcepted.get());
		}
		return filteredList;

	}

}
