package edu.emory.cci.bindaas.core.exception;

public class ProviderNotFoundException extends Exception{

	
	private static final long serialVersionUID = -8418792888359075292L;
	private String providerId;
	private int version;

	public ProviderNotFoundException(String providerId , int providerVersion) {
		super();
		this.providerId = providerId;
		this.version = providerVersion;
		
	}

	public ProviderNotFoundException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public ProviderNotFoundException(String message) {
		super(message);
		
	}

	public ProviderNotFoundException(Throwable cause) {
		super(cause);
		
	}

	@Override
	public String getMessage() {
		
		return "Could not locate Provider id=[" + providerId + "] version=[" + version + "]";
	}

	
	
}
