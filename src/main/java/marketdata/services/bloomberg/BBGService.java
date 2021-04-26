package marketdata.services.bloomberg.services;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;

import exceptions.DataServiceStartException;
import finance.identifiers.Identifier;
import finance.instruments.IInstrument;
import finance.instruments.InstrumentFactory;
import marketdata.services.base.AbstractDataService;
import marketdata.services.base.DataServiceEnum;
import marketdata.services.bloomberg.enumeration.ServiceType;
import marketdata.services.bloomberg.enumeration.TickerSuffix;

public abstract class BBGService extends AbstractDataService {

	@Autowired
	InstrumentFactory factory;
	@Value("${bloomberg.serverHost}")
	private String serverHost;
	@Value("${bloomberg.serverPort}")
	private Integer serverPort;
	private Session session;
	private SessionOptions sessionOptions;
	private Service service;
	private ServiceType serviceType;
	private volatile boolean opened = false;
	
	public BBGService(ServiceType serviceType) {
		super(DataServiceEnum.BLOOMBERG);
		this.serviceType = serviceType;
	}
	
	@PostConstruct
	public void init() {
		this.sessionOptions = new SessionOptions();
		this.sessionOptions.setServerHost(serverHost);
		this.sessionOptions.setServerPort(serverPort);
	}
	
	public synchronized boolean isOpened() {
		return this.opened;
	}
	
	public synchronized void setOpened(boolean opened) {
		this.opened = opened;
		synchronized(this) {
			this.notifyAll();
		}
	}
	
	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public SessionOptions getSessionOptions() {
		return sessionOptions;
	}
	
	public void setSessionOptions(SessionOptions sessionOptions) {
		this.sessionOptions = sessionOptions;
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}
	 
	public Service getService() {
		return service;
	}
	
	public void setService(Service service) {
		this.service = service;
	}
	
	public void start() throws DataServiceStartException {
		try {
			if(!this.opened) {
				Logger.getRootLogger().log(Level.INFO,"Connecting to Bloomberg Data Service...");
				session.startAsync();
				waitForServiceStatusUpdate();
				Logger.getRootLogger().log(Level.INFO, "Now connected to Bloomberg Data Service.");
			}
		} catch (IOException e) {
			throw new DataServiceStartException("Bloomberg Service start failed:",e);
		}
	}
	
	public void stop() {
		try {
			session.stop(Session.StopOption.ASYNC);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void waitForServiceStatusUpdate() {
		synchronized (this) {
			try {
				while(!this.isOpened()) {
					this.wait();
				}
				
				Service service = session.getService(this.getServiceType().getSyntax());
				this.setService(service);
			} catch (Exception e) {
				// TODO: BBG Service connection Time out ?
			}
			
		}
	}
	
	public Instant convertBbgToJavaInstant(Datetime bbgdt) {
		int year = bbgdt.year();
		int month = bbgdt.month();
		int day = bbgdt.dayOfMonth();
		int hour = bbgdt.hour();
		int minute = bbgdt.minute();
		int second = bbgdt.second();
		int milli = bbgdt.millisecond();
		
		ZonedDateTime javadt = ZonedDateTime.of(year,month,day,hour,minute,second,milli,ZoneId.of("UTC"));
		return javadt.toInstant();
	}
	
	public LocalDate convertBbgToJavaLocalDate(Datetime bbgdt) {
		int year = bbgdt.year();
		int month = bbgdt.month();
		int day = bbgdt.dayOfMonth();
		LocalDate javadt = LocalDate.of(year, month, day);
		
		return javadt;
	}
	
	public String convertJavaZonedDateTimeToBBGString(ZonedDateTime javadt) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter timef = DateTimeFormatter.ofPattern("HH:mm:ss");
		String bbgDt = dtf.format(javadt.withZoneSameInstant(ZoneId.of("UTC")));
		String bbgTime = timef.format(javadt.withZoneSameInstant(ZoneId.of("UTC")));
		String output = bbgDt + "T" + bbgTime;
		
		return output;
	}
	
	public static String convertJavaLocalDateToBBGString(LocalDate javadt) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
		String bbgdt = dtf.format(javadt);
		return bbgdt;
	}
	
	public String getIdentifierStringFromBloombergResponse(String expression) {
		String text[] = expression.split("/",3);
		String idstr = text[2];
		for(TickerSuffix suffix : TickerSuffix.values()) {
			int suffixIdx = idstr.indexOf(suffix.toString());
			if(suffixIdx > -1) {
				idstr = idstr.substring(0,suffixIdx-1);
				break;
			}
		}
		
		return idstr;
	}
	
	public Identifier getIdentifierFromBloombergResponse(String expression) {
		String idstr = getIdentifierStringFromBloombergResponse(expression);
		Identifier id = (Identifier) factory.getIdentifier(idstr);
		return id;
	}
	
	public IInstrument getInstrumentFromBloombergResponse(String expression) {
		IInstrument instrument = null;
		Identifier id = getIdentifierFromBloombergResponse(expression);
		instrument = id.getInstrument();
		return instrument;
	}
}
