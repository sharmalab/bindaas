package edu.emory.cci.bindaas.core.apikey.api;

public class APIKeyManagerException extends Exception{
	private static final long serialVersionUID = 1L;
	private Reason reason;
	public static enum Reason {KEY_DOES_NOT_EXIST , KEY_ALREADY_EXIST, PROCESSING_ERROR, METHOD_NOT_IMPLEMENTED}
	
	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	public APIKeyManagerException(Reason reason) {
		super();
		this.reason = reason;

	}

	public APIKeyManagerException(String arg0, Throwable arg1 , Reason reason) {
		super(arg0, arg1);
		this.reason = reason;
	}

	public APIKeyManagerException(String arg0 , Reason reason) {
		super(arg0);
		this.reason = reason;

	}

	public APIKeyManagerException(Throwable arg0 , Reason reason) {
		super(arg0);
		this.reason = reason;

	}

}
