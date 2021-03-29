package finance.order;

public enum TradeSide {
	BUY(1),
	SELL(-1);
	
	private int sign;
	
	private TradeSide(int sign) {
		this.sign = sign;
	}
	
	public int getSign() { return sign; }
	
	public TradeSide flip() {
		return (this.equals(TradeSide.BUY)) ? TradeSide.SELL : TradeSide.BUY;
	}
	
	public static TradeSide get(int sign) {
		for(TradeSide s : values()) {
			if(s.sign == sign) return s;
		}
		return null;
	}
}
