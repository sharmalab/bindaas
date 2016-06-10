package edu.emory.cci.bindaas.framework.provider.exception;

public class QueryExecutionFailedException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	public QueryExecutionFailedException(String providerId,
			Integer providerVersion, String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
		
	}

	public QueryExecutionFailedException(String providerId,
			Integer providerVersion, String arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public QueryExecutionFailedException(String providerId,
			Integer providerVersion, Throwable arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public QueryExecutionFailedException(String providerId,
			Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	@Override
	public Integer getHttpStatusCode() {
		return AbstractHttpCodeException.API_EXECUTION_ERROR;
	}
	
	@Override
	public String getErrorDescription() {
		return "Execution of the Query API failed";
	}

	@Override
	public String getSuggestedAction() {
		return "Check the syntax of Query and/or Query parameters";
	}


}
