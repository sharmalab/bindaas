package edu.emory.cci.bindaas.framework.provider.exception;

public class BadContentException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	@Override
	public Integer getHttpStatusCode() {
		
		return AbstractHttpCodeException.BAD_CONTENT_ERROR;
	}

	public BadContentException(String providerId, Integer providerVersion,
			String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
	
	}

	public BadContentException(String providerId, Integer providerVersion,
			String arg0) {
		super(providerId, providerVersion, arg0);
	
	}

	public BadContentException(String providerId, Integer providerVersion,
			Throwable arg0) {
		super(providerId, providerVersion, arg0);
	
	}

	public BadContentException(String providerId, Integer providerVersion) {
		super(providerId, providerVersion);
	}

	@Override
	public String getErrorDescription() {
		return "This error indicates that either the format or the content of input data is incorrect";
	}

	@Override
	public String getSuggestedAction() {
		return "Please check the format/content of input data. If error persist please contact system administrator";
	}
	
	

}
