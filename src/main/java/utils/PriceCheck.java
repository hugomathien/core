package utils;

public class PriceCheck {

	public static boolean validPrice(Double price) {
		if(price == null)
			return false;
		if(price == 0.0 || price.isNaN() || price.isInfinite())
			return false;
		return true;
	}
}
