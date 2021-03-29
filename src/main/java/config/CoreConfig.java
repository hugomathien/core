package config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import eventprocessors.EventPriorityQueue;
import events.MarketDataEventFactory;
import finance.identifiers.IdentifierType;
import finance.instruments.ETF;
import finance.instruments.FX;
import finance.instruments.Future;
import finance.instruments.IInstrument;
import finance.instruments.Index;
import finance.instruments.InstrumentFactory;
import finance.instruments.InstrumentType;
import finance.instruments.SingleStock;
import finance.springBean.Exchange;
import marketdata.services.bloomberg.responsehandler.BBGRealTimeResponseHandler;
import marketdata.services.bloomberg.responsehandler.BBGReferenceResponseHandler;
import marketdata.services.bloomberg.services.BBGRealTimeDataService;
import marketdata.services.bloomberg.services.BBGReferenceDataService;
import utils.Spark;

@Configuration
@Import({FlatFileDataConfig.class,SparkConfig.class})
@ImportResource({"classpath:/config/bart.core.config.xml"})
@ComponentScan(basePackages = {
		"events",
		"eventprocessors",
		"marketdata",
		"finance.instruments",
		"marketdata.field",
		"marketdata.services",
		"springBean"})
public class CoreConfig implements ApplicationContextAware {
	public static ApplicationContext ctx;
	public static IdentifierType PRIMARY_IDENTIFIER_TYPE;
	public static ZoneId GLOBAL_ZONE_ID = ZoneId.systemDefault();
	public static LocalDate GLOBAL_START_DATE;
	public static LocalDate GLOBAL_END_DATE; 
	
	@Autowired
	InstrumentFactory factory;
	@Autowired(required = false)
	public Map<String,Exchange> exchangeMicMap;
	@Autowired
	public ApplicationEventPublisher publisher;
	@Autowired
	public EventPriorityQueue eventQueue;
	
	public static CoreConfig services() {
		return ctx.getBean(CoreConfig.class);
	}
	
	public Spark spark() {
		return ctx.getBean(Spark.class);
	}
	
	public SparkSession sparkSession() {
		return ctx.getBean(SparkSession.class);
	}
	
	
	public InstrumentFactory instrumentFactory() {
		return ctx.getBean(InstrumentFactory.class);
	}
	
	public MarketDataEventFactory marketDataEventFactory() {
		return ctx.getBean(MarketDataEventFactory.class);
	}
	
	public EventPriorityQueue eventPriorityQueue() {
		return ctx.getBean(EventPriorityQueue.class);
	}
	
	public Exchange getExchange(String mic) {
		if(mic == null)
			return exchangeMicMap.get("OTC");
		
		if(exchangeMicMap.containsKey(mic))
			return exchangeMicMap.get(mic);
		else
			return new Exchange(mic);
	}
	
	public IInstrument getInstrument(String id) {
		return factory.getInstrument(id);
	}
	
	public SingleStock getSingleStock(String id) {
		if(factory.hasInstrument(id,InstrumentType.SingleStock))
			return (SingleStock) factory.getInstrument(id);
		else
			return (SingleStock) factory.makeInstrument(InstrumentType.SingleStock, id);
	}
	
	public Future getOrMakeFuture(String id) {
		if(factory.hasInstrument(id,InstrumentType.Future))
			return (Future) factory.getInstrument(id);
		else
			return (Future) factory.makeInstrument(InstrumentType.Future, id);
	}
	
	public ETF getOrMakeETF(String id) {
		if(factory.hasInstrument(id,InstrumentType.ETF))
			return (ETF) factory.getInstrument(id);
		else
			return (ETF) factory.makeInstrument(InstrumentType.ETF, id);
	}
	
	public Index getOrMakeIndex(String id) {
		if(factory.hasInstrument(id,InstrumentType.Index))
			return (Index) factory.getInstrument(id);
		else
			return (Index) factory.makeInstrument(InstrumentType.Index, id);
	}
	
	public FX getOrMakeFx(String ccyLeft,String ccyRight) {
		if(factory.hasFx(ccyLeft,ccyRight))
			return factory.getFx(ccyLeft,ccyRight);
		else
			return factory.makeFx(ccyLeft,ccyRight);
	}
	
	public BBGReferenceDataService bloombergReferenceDataService() {
		return ctx.getBean(BBGReferenceDataService.class);
	}
	
	public BBGRealTimeDataService bloombergRealTimeDataService() {
		return ctx.getBean(BBGRealTimeDataService.class);
	}
	
	public BBGReferenceResponseHandler bloombergReferenceResponseHandler() {
		return ctx.getBean(BBGReferenceResponseHandler.class);
	}
	
	public BBGRealTimeResponseHandler bloombergRealTimeResponseHandler() {
		return ctx.getBean(BBGRealTimeResponseHandler.class);
	}
	
	public void setGlobalStartDate(String startDate) {
		GLOBAL_START_DATE = LocalDate.parse(startDate);
	}
	
	public void setGlobalEndDate(String endDate) {
		GLOBAL_END_DATE = LocalDate.parse(endDate);
	}
	
	public void setGlobalZoneId(String zoneId) {
		GLOBAL_ZONE_ID = ZoneId.of(zoneId);
	}
	
	public void setPrimaryIdentifierType(String primaryIdentifierType) {
		PRIMARY_IDENTIFIER_TYPE = IdentifierType.valueOf(primaryIdentifierType);
	}
	
	public void run() {
		eventQueue.pollAndPublishAll();
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ctx = applicationContext;
	}
	
	
}
