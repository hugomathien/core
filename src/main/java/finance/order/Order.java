package finance.order;

import java.time.Instant;

import finance.instruments.IInstrument;

public class Order {
	private String orderID;
	private Channel channel;
	private OrderOrigination orderOrigination;
	private IInstrument instrument;
	private long quantity;
	private double price;
	private String currency;
	private Instant timestamp;
	private TradeSide side;
	
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public OrderOrigination getOrderOrigination() {
		return orderOrigination;
	}
	public void setOrderOrigination(OrderOrigination orderOrigination) {
		this.orderOrigination = orderOrigination;
	}
	public IInstrument getInstrument() {
		return instrument;
	}
	public void setInstrument(IInstrument instrument) {
		this.instrument = instrument;
	}
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Instant getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}
	public TradeSide getSide() {
		return side;
	}
	public void setSide(TradeSide side) {
		this.side = side;
	}
}
