package edu.emory.cci.bindaas.framework.provider.exception;

public class SubmitExecutionFailedException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	public SubmitExecutionFailedException(String providerId,
			Integer providerVersion, String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
		
	}

	public SubmitExecutionFailedException(String providerId,
			Integer providerVersion, String arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public SubmitExecutionFailedException(String providerId,
			Integer providerVersion, Throwable arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public SubmitExecutionFailedException(String providerId,
			Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	@Override
	public Integer getHttpStatusCode() {
		
		 return AbstractHttpCodeException.API_EXECUTION_ERROR;
	}
	
	@Override
	public String getErrorDescription() {
		return "Execution of the Submit API failed";
	}

	@Override
	public String getSuggestedAction() {
		return "Check the input data";
	}


}
