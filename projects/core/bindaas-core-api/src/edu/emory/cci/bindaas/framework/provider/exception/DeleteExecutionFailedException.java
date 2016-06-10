package edu.emory.cci.bindaas.framework.provider.exception;

public class DeleteExecutionFailedException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	@Override
	public Integer getHttpStatusCode() {
		
		return AbstractHttpCodeException.API_EXECUTION_ERROR;
	}

	public DeleteExecutionFailedException(String providerId, Integer providerVersion,
			String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
	
	}

	public DeleteExecutionFailedException(String providerId, Integer providerVersion,
			String arg0) {
		super(providerId, providerVersion, arg0);
	
	}

	public DeleteExecutionFailedException(String providerId, Integer providerVersion,
			Throwable arg0) {
		super(providerId, providerVersion, arg0);
	
	}

	public DeleteExecutionFailedException(String providerId, Integer providerVersion) {
		super(providerId, providerVersion);
	}

	@Override
	public String getErrorDescription() {
		return "Execution of the Delete API failed";
	}

	@Override
	public String getSuggestedAction() {
		return "Check the syntax of Delete Query and/or query parameters";
	}
}
