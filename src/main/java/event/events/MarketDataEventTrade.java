package event.events;

import java.time.Instant;
import java.time.temporal.Temporal;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import event.sequencing.processing.CoreEventType;
import finance.instruments.IInstrument;
import marketdata.container.Tick;
import marketdata.field.Field;
import marketdata.services.base.DataServiceEnum;

@Component
@Scope("prototype")
@Lazy(true)
public class MarketDataEventTrade extends MarketDataEvent<Tick> {

	private Field priceField;
	private Field sizeField;
	private Double price;
	private Long size;
	private String cc;
	private String bmc;
	private String bc;
	private String ec;
	private String rpsc;
	
	public MarketDataEventTrade(
			Instant eventTimestamp,
			DataServiceEnum dataService,
			Temporal marketDataStart,
			Temporal marketDataEnd,
			IInstrument instrument,
			Field field,
			Object value) {
		super(eventTimestamp, CoreEventType.TRADE, dataService, marketDataStart, marketDataEnd, instrument, field, value);
	}

	public Field getPriceField() {
		return priceField;
	}

	public void setPriceField(Field priceField) {
		this.priceField = priceField;
	}

	public Field getSizeField() {
		return sizeField;
	}

	public void setSizeField(Field sizeField) {
		this.sizeField = sizeField;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBmc() {
		return bmc;
	}

	public void setBmc(String bmc) {
		this.bmc = bmc;
	}

	public String getBc() {
		return bc;
	}

	public void setBc(String bc) {
		this.bc = bc;
	}

	public String getEc() {
		return ec;
	}

	public void setEc(String ec) {
		this.ec = ec;
	}

	public String getRpsc() {
		return rpsc;
	}

	public void setRpsc(String rpsc) {
		this.rpsc = rpsc;
	}
				
}
