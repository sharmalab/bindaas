package edu.emory.cci.bindaas.framework.provider.exception;

public class MethodNotImplementedException extends AbstractHttpCodeException {
	private static final long serialVersionUID = 1L;
	public MethodNotImplementedException(String providerId,
			Integer providerVersion, String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
		
	}

	public MethodNotImplementedException(String providerId,
			Integer providerVersion, String arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public MethodNotImplementedException(String providerId,
			Integer providerVersion, Throwable arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public MethodNotImplementedException(String providerId,
			Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	@Override
	public Integer getHttpStatusCode() {

		return 501;
	}

	@Override
	public String getErrorDescription() {
		
		return "Method Not Implemented";
	}

	@Override
	public String getSuggestedAction() {
		return "Please check the documentation of the Provider plugin";
	}

}
