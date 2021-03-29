package marketdata;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.Temporal;

import utils.TimeSeries;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import finance.instruments.Instrument;
import marketdata.container.AbstractMarketDataContainer;
import marketdata.container.Bar;
import marketdata.container.Day;
import marketdata.container.Tick;
import marketdata.field.Field;
import utils.PriceCheck;

public class MarketDataFunctions {

	public static Function<Instrument,TimeSeries<Instant,Tick>> getTickTimeSeries = 
			t -> t.getMarketData().getHistorical().getTickData().getTimeSeries();


	public static Function<Instrument,TimeSeries<LocalDate,Day>> getEodTimeSeries = 
			t -> t.getMarketData().getHistorical().getEodData().getTimeSeries();


	public static BiFunction<Instrument,Integer,TimeSeries<Instant,Bar>> getBarTimeSeries = 
			(t,u) -> t.getMarketData().getHistorical().getBarData().getTimeSeries(u);
			
	public static BiFunction<TimeSeries,Temporal,AbstractMarketDataContainer> getMarketDataContainer = 
			(t,u) -> ((TimeSeries<Temporal, AbstractMarketDataContainer>) t).get(u);
			
	public static BiFunction<TimeSeries,Temporal,AbstractMarketDataContainer> floorMarketDataContainer = 
			(t,u) -> ((TimeSeries<Temporal, AbstractMarketDataContainer>) t).floorEntry(u).getValue();
					
	public static BiFunction<TimeSeries,Temporal,AbstractMarketDataContainer> ceilMarketDataContainer = 
			(t,u) -> ((TimeSeries<Temporal, AbstractMarketDataContainer>) t).ceilingEntry(u).getValue();
			
	public static BiFunction<AbstractMarketDataContainer,Field,Object> getFieldValue = 
			(t,u) -> t.get(u);
			
	public static BiFunction<TimeSeries, Field, TimeSeries<Temporal, AbstractMarketDataContainer>> isNotMissing = 
			new BiFunction<TimeSeries,Field,TimeSeries<Temporal,AbstractMarketDataContainer>>() {
		
		@Override
		public TimeSeries<Temporal, AbstractMarketDataContainer> apply(TimeSeries ts,Field f) {
			@SuppressWarnings("unchecked")
			Map<Temporal,AbstractMarketDataContainer> map = ((TimeSeries<Temporal,AbstractMarketDataContainer>) ts).entrySet()
					.stream()
					.filter(x -> x.getValue().getFieldsMap().containsKey(f))
					.collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue()));
			
			TimeSeries<Temporal,AbstractMarketDataContainer> tsFiltered = new TimeSeries<Temporal,AbstractMarketDataContainer>();
			tsFiltered.putAll(map);
			return tsFiltered;				
		}		
	};
	
	public static BiFunction<TimeSeries<Temporal,AbstractMarketDataContainer>,Field,TimeSeries<Temporal,AbstractMarketDataContainer>> isValidPrice = 
			new BiFunction<TimeSeries<Temporal,AbstractMarketDataContainer>,Field,TimeSeries<Temporal,AbstractMarketDataContainer>>() {
		
		@Override
		public TimeSeries<Temporal,AbstractMarketDataContainer> apply(TimeSeries ts,Field f) {
			Map<Temporal,AbstractMarketDataContainer> map = ((TimeSeries<Temporal,AbstractMarketDataContainer>) ts).entrySet()
					.stream()
					.filter(x -> x.getValue().getFieldsMap().containsKey(f))
					.filter(x -> PriceCheck.validPrice((Double) x.getValue().get(f)))
					.collect(Collectors.toMap((entry) -> entry.getKey(),(entry) -> entry.getValue()));
			
			TimeSeries<Temporal,AbstractMarketDataContainer> tsFiltered = new TimeSeries<Temporal,AbstractMarketDataContainer>();
			tsFiltered.putAll(map);
			return tsFiltered;				
		}		
	};
	
}
