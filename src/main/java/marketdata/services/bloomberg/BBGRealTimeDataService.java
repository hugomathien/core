package marketdata.services.bloomberg;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;

import exceptions.DataQueryException;
import finance.identifiers.IBloombergIdentifier;
import finance.identifiers.Identifier;
import marketdata.field.Field;
import marketdata.services.base.DataRequest;
import marketdata.services.base.IRealTimeDataService;
import marketdata.services.bloomberg.enumeration.ServiceType;
import marketdata.services.bloomberg.responsehandler.BBGRealTimeResponseHandler;

@Service("BLOOMBERG_REALTIME")
@Scope("singleton")
@Lazy(true)
public class BBGRealTimeDataService extends BBGService implements IRealTimeDataService {

	private HashMap<CorrelationID,Pair<Identifier,Collection<Field>>> requestMap;
	@Autowired
	private BBGRealTimeResponseHandler responseHandler;

	@PostConstruct
	public void init() {
		this.requestMap = new HashMap<CorrelationID, Pair<Identifier,Collection<Field>>>();
		Session session = new Session(this.getSessionOptions(), responseHandler);
		this.setSession(session);
	}

	public BBGRealTimeDataService() {
		super(ServiceType.LIVE_DATA);
	}

	public void query(DataRequest<?> requestBuilder) throws DataQueryException {
		SubscriptionList subscriptions = new SubscriptionList();

		String fieldChain = requestBuilder
				.getFields()
				.stream()
				.map(f -> f.toString()).reduce((a,b) -> a+","+b).get();

		requestBuilder
		.getIdentifiers()
		.forEach(new Consumer<Identifier>() {
			@Override
			public void accept(Identifier id) {
				CorrelationID correlID = new CorrelationID(requestMap.size()+1);
				Subscription sub = new Subscription(
						((IBloombergIdentifier) id).getBbgQuerySyntax(),
						fieldChain,
						new CorrelationID(requestMap.size() + 1));
				subscriptions.add(sub);
				requestMap.put(correlID, new Pair<Identifier,Collection<Field>>(id,requestBuilder.getFields()));
			}
		});

		try {
			this.getSession().subscribe(subscriptions);
		} catch(IOException e) {
			throw new DataQueryException("Bloomberg Real Time Service query failed", e);			
		}
	}
	
	public HashMap<CorrelationID,Pair<Identifier, Collection<Field>>> getRequestMap() {
		return requestMap;
	}
	
	public void setRequestMap(HashMap<CorrelationID,Pair<Identifier,Collection<Field>>> requestMap) {
		this.requestMap = requestMap;
	}
	
	public BBGRealTimeResponseHandler getResponseHandler() {
		return responseHandler;
	}
	
	
}
