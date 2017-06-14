package edu.emory.cci.bindaas.framework.provider.exception;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.framework.model.ProviderException;

public abstract class AbstractHttpCodeException extends ProviderException {

	private static final long serialVersionUID = 1L;
	/**
	 * Custom Error Codes 4xx
	 */
	
	/**
	 * 
	 */
	

	/**
	 * Error code to denote error in API execution for whatever reason
	 */
	public final static Integer API_EXECUTION_ERROR = 571;
	
	/**
	 * Error code to denote conditions where data is assumed to have some format and is not.
	 */
	public final static Integer BAD_CONTENT_ERROR = 572;
	
	/**
	 * Error code to denote conditions where provider fails to connect to remote server or database
	 */
	public final static Integer NETWORK_CONNECTION_ERROR = 573;
	
	/**
	 * Error code to denote conditions where some assertion made about the upstream content fails
	 */
	
	public final static Integer UPSTREAM_CONTENT_ASSERTION_FAILED = 574;

	/**
	 * Error code to denote conditions where validation of API parameter fails
	 */
	
	public final static Integer VALIDATION_ERROR = 575;
	
	/**
	 * Error code to denote error on the part of API consumer. Indicates one or many mandatory query parameter are missing
	 */
	
	public final static Integer MISSING_MANDATORY_QUERY_PARAMETER_ERROR = 471;
	
	
	
	
	public abstract String getErrorDescription();
	public abstract String getSuggestedAction();
	
	
	public AbstractHttpCodeException(String providerId, Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	public AbstractHttpCodeException(String providerId,
			Integer providerVersion, String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
	
	}

	public AbstractHttpCodeException(String providerId,
			Integer providerVersion, String arg0) {
		super(providerId, providerVersion, arg0);
	
	}

	public AbstractHttpCodeException(String providerId,
			Integer providerVersion, Throwable arg0) {
		super(providerId, providerVersion, arg0);
	}

	public abstract Integer getHttpStatusCode();
	
	public JsonObject toJson()
	{
		JsonObject responseObj = new JsonObject();
		responseObj.add("errorDescription",  new JsonPrimitive(this.getErrorDescription()));
		responseObj.add("suggestedAction",  new JsonPrimitive(this.getSuggestedAction()));
		responseObj.add("detailedMessage",  new JsonPrimitive(this.getMessage()));
		return responseObj;
	}
	
	public String toString()
	{
		JsonObject responseObj = new JsonObject();
		responseObj.add("errorDescription",  new JsonPrimitive(this.getErrorDescription()));
		responseObj.add("suggestedAction",  new JsonPrimitive(this.getSuggestedAction()));
		responseObj.add("detailedMessage",  new JsonPrimitive(this.getMessage()));
		return responseObj.toString();
	}
}
