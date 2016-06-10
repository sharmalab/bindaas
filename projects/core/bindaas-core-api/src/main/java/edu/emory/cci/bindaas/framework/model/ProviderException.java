package edu.emory.cci.bindaas.framework.model;

// TODO : Add provider information in the constructor of the class to more descriptive logging
public class ProviderException extends Exception {

	private String providerId;
	private Integer providerVersion;
	
	public ProviderException(String providerId , Integer providerVersion) {
		
		this.providerId = providerId;
		this.providerVersion = providerVersion;
	}

	public ProviderException(String providerId , Integer providerVersion,String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.providerId = providerId;
		this.providerVersion = providerVersion;
	}

	public ProviderException(String providerId , Integer providerVersion,String arg0) {
		super(arg0);
		this.providerId = providerId;
		this.providerVersion = providerVersion;
		
	}

	public ProviderException(String providerId , Integer providerVersion,Throwable arg0) {
		super(arg0);
		this.providerId = providerId;
		this.providerVersion = providerVersion;
		
	}

	private static final long serialVersionUID = 9087532630302437326L;
	
	public String getMessage()
	{
		return "providerId=[" + providerId + "] version=[" + providerVersion + "] : " + super.getMessage();
	}

}
