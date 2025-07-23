package config;

import java.time.LocalDate;
import java.time.ZoneId;

import event.timers.TimerDataframeWriterBatch;
import event.timers.TimerDataframeWriterStream;
import event.timers.Clock;
import event.timers.TimerDataRequest;
import event.timers.TimerStateToDataframe;
import finance.misc.ExchangeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;

import event.events.MarketDataEventFactory;
import event.processing.EventPriorityQueue;
import finance.identifiers.IdentifierType;
import finance.instruments.ETF;
import finance.instruments.FX;
import finance.instruments.Future;
import finance.instruments.IInstrument;
import finance.instruments.Index;
import finance.instruments.InstrumentFactory;
import finance.instruments.InstrumentType;
import finance.instruments.SingleStock;
import finance.misc.Exchange;
import marketdata.services.bloomberg.BBGRealTimeDataService;
import marketdata.services.bloomberg.BBGReferenceDataService;
import marketdata.services.bloomberg.BBGRealTimeResponseHandler;
import marketdata.services.bloomberg.BBGReferenceResponseHandler;
import marketdata.services.randomgen.RandomGeneratorReferenceDataService;
import marketdata.services.randomgen.RandomGeneratorReferenceResponseHandler;
import marketdata.services.flatfile.FlatFileReferenceDataService;
import marketdata.services.flatfile.FlatFileReferenceResponseHandler;
import utils.Spark;

@Configuration
@ImportResource({"${config.core}"})
@Import({SparkConfig.class, FieldConfig.class})
@ComponentScan(basePackages = {
		"event.events",
		"event.processing",
		"event.sequencing",
		"marketdata",
		"finance.misc",
		"finance.instruments",
		"marketdata.field",
		"marketdata.services",
		"marketdata.services.bloomberg",
		"marketdata.services.flatfile",
		"marketdata.services.randomgen"})
public class CoreConfig implements ApplicationContextAware {
	public static ApplicationContext ctx;
	public static IdentifierType PRIMARY_IDENTIFIER_TYPE;
	public static ZoneId GLOBAL_ZONE_ID = ZoneId.systemDefault();
	public static LocalDate GLOBAL_START_DATE;
	public static LocalDate GLOBAL_END_DATE;
	public static String GLOBAL_REFERENCE_CURRENCY;
	public static Logger logger = LogManager.getLogger();

	@Autowired
	InstrumentFactory factory;
	@Autowired
	ExchangeFactory exchangeFactory;
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
	
	@Bean
	@Lazy
	@Scope("prototype")
	public TimerDataRequest getDataRequestSequencer(TimerDataRequest.Builder builder) {
		return new TimerDataRequest(builder);
	}

	@Bean
	@Lazy
	@Scope("prototype")
	public TimerStateToDataframe getInstrumentStateCaptureSequencer(TimerStateToDataframe.Builder builder) {
		return new TimerStateToDataframe(builder);
	}

	@Bean
	@Lazy
	@Scope("prototype")
	public TimerDataframeWriterBatch getDfWriterBatch(TimerDataframeWriterBatch.Builder builder) {
		return new TimerDataframeWriterBatch(builder);
	}

	@Bean
	@Lazy
	@Scope("prototype")
	public TimerDataframeWriterStream getDfWriterStream(TimerDataframeWriterStream.Builder builder) {
		return new TimerDataframeWriterStream(builder);
	}

	@Bean
	@Lazy
	@Scope("prototype")
	public dataset.StateToDataframe getInstrumentStateCapture(dataset.StateToDataframe.Builder builder) {
		return new dataset.StateToDataframe(builder);
	}

	@Bean
	@Lazy
	@Scope("prototype")
	public Clock getClock() {
		return new Clock();
	}


	public Exchange getExchange(String exchangeCode) {
		return this.exchangeFactory.getExchange(exchangeCode);
	}
	
	
	// TODO: Create get or make instrument
	public IInstrument getInstrument(String id) {
		return factory.getInstrument(id);
	}
	public IInstrument getOrMakeInstrument(InstrumentType instrumentType,String id) {
		return factory.makeInstrument(instrumentType,id);
	}

	public IInstrument getOrMakeInstrument(InstrumentType instrumentType,IdentifierType idType,String id) {
		return factory.makeInstrument(instrumentType,idType,id);
	}


	public SingleStock getOrMakeSingleStock(String id) {
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

	public FX getOrMakeFx(String ccyPair) {
		return factory.makeFx(ccyPair);
	}


	public FX getOrMakeFx(String ccyLeft,String ccyRight) {
		return factory.makeFx(ccyLeft,ccyRight);
	}
	
	
	public RandomGeneratorReferenceDataService randomGeneratorReferenceDataService() {
		return ctx.getBean(RandomGeneratorReferenceDataService.class);
	}
	
	public RandomGeneratorReferenceResponseHandler randomGeneratorReferenceResponseHandler() {
		return ctx.getBean(RandomGeneratorReferenceResponseHandler.class);
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

	public FlatFileReferenceDataService flatFileReferenceDataService() {
		return ctx.getBean(FlatFileReferenceDataService.class);
	}
	public FlatFileReferenceResponseHandler flatFileReferenceResponseHandler() {
		return ctx.getBean(FlatFileReferenceResponseHandler.class);
	}

	@Value("${config.clock.startDate}")
	public void setGlobalStartDate(String startDate) {
		GLOBAL_START_DATE = LocalDate.parse(startDate);
	}
	
	@Value("${config.clock.endDate}")
	public void setGlobalEndDate(String endDate) {
		GLOBAL_END_DATE = LocalDate.parse(endDate);
	}
	
	@Value("${config.clock.zoneId}")
	public void setGlobalZoneId(String zoneId) {
		GLOBAL_ZONE_ID = ZoneId.of(zoneId);
	}

	@Value("${config.reference_currency}")
	public void setGlobalReferenceCurrency(String reference_currency) {
		GLOBAL_REFERENCE_CURRENCY = reference_currency;
	}
	
	@Value("${config.instrument.primary_identifier_type}")
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
