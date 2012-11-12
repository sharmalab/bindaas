package edu.emory.cci.bindaas.framework.model;

// TODO : Add provider information in the constructor of the class to more descriptive logging
public class ProviderException extends Exception {

	public ProviderException() {
		super();
		
	}

	public ProviderException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

	public ProviderException(String arg0) {
		super(arg0);
		
	}

	public ProviderException(Throwable arg0) {
		super(arg0);
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9087532630302437326L;

}
