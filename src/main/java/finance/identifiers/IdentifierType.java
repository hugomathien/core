package finance.identifiers;

import static finance.identifiers.IdentifierType.RIC;

import java.io.Serializable;

import utils.MiscUtils;

public enum IdentifierType implements Serializable {
	TICKER,RIC,SEDOL;

	public static boolean isDefined(String str) {
		try {
			IdentifierType.valueOf(str);
			return true;
		}
		catch(IllegalArgumentException e) {
			return false;
		}
	}

	public static IdentifierType guess(String name) {
		if(name.contains("."))
			return IdentifierType.RIC;
		else if(name.contains(" "))
			return IdentifierType.TICKER;
		else if(MiscUtils.onlyDigits(name) && name.length() == 7)
			return IdentifierType.SEDOL;
		else
			return IdentifierType.TICKER;
	}
	
	public static IdentifierType guessFX(String name) {
		if(name.contains("="))
			return IdentifierType.RIC;
		else
			return IdentifierType.TICKER;
	}

}
