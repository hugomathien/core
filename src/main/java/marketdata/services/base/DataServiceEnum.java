package marketdata.services.base;

public enum DataServiceEnum {
RANDOMGEN("RANDOMGEN_REFERENCE","RANDOMGEN_REALTIME",false),
BLOOMBERG("BLOOMBERG_REFERENCE","BLOOMBERG_REALTIME",true),
FLAT_FILE("FLAT_FILE_REFERENCE","FLAT_FILE_REALTIME",false);

private String reference;
private String realTime;
private boolean asynchronous;

	DataServiceEnum(String reference,String realtime,boolean asynchronous) {
		this.reference = reference;
		this.realTime = realtime;
		this.asynchronous = asynchronous;
	}
	
	public String getReference() {
		return reference;
	}
	
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	public String getRealTime() {
		return realTime;
	}
	
	public void setRealTime(String realTime) {
		this.realTime = realTime;
	}
	
	public boolean isAsynchronous() {
		return asynchronous;
	}
	
	public void setAsynchronous(boolean asynchronous) {
		this.asynchronous = asynchronous;
	}
}
