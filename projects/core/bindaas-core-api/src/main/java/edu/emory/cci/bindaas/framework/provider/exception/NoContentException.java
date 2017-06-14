package edu.emory.cci.bindaas.framework.provider.exception;

public class NoContentException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	public NoContentException(String providerId, Integer providerVersion,
			String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
		
	}

	public NoContentException(String providerId, Integer providerVersion,
			String arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public NoContentException(String providerId, Integer providerVersion,
			Throwable arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public NoContentException(String providerId, Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	@Override
	public Integer getHttpStatusCode() {
		
		return 204;
	}

	@Override
	public String getErrorDescription() {
		return "Query execution was successfull but did not return any result";
	}

	@Override
	public String getSuggestedAction() {
		return "Do nothing";
	}

}
