package edu.emory.cci.bindaas.framework.provider.exception;

public class ValidationException extends AbstractHttpCodeException {
	private static final long serialVersionUID = 1L;
	public ValidationException(String providerId, Integer providerVersion,
			String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
		
	}

	public ValidationException(String providerId, Integer providerVersion,
			String arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public ValidationException(String providerId, Integer providerVersion,
			Throwable arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public ValidationException(String providerId, Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	@Override
	public String getErrorDescription() {
		
		return "Indicates validation of parameters specified by API creator failed";
	}

	@Override
	public String getSuggestedAction() {
		
		return "Please check various parameters of the API";
	}

	@Override
	public Integer getHttpStatusCode() {

		return AbstractHttpCodeException.VALIDATION_ERROR;
	}

}
