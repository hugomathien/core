package marketdata.services.bloomberg.utils;

public enum ServiceType {
	REFERENCE_DATA("//blp/refdata"),
	LIVE_DATA("//blp/mktdata");

	private String syntax;
	
	private ServiceType(String syntax) {
		this.syntax = syntax;
	}
	
	public String getSyntax() {
		return syntax;
	}
}
