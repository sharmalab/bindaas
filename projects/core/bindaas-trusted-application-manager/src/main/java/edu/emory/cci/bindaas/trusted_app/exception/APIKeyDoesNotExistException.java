package edu.emory.cci.bindaas.trusted_app.exception;

import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public class APIKeyDoesNotExistException  extends AbstractHttpCodeException {

	private String user;
	public final static Integer HTTP_ERROR_CODE = 591;
	
	public APIKeyDoesNotExistException(String user)
	{
		super("edu.emory.cci.bindaas.trusted_app", 1);
		this.user = user;
	}
	
	private static final long serialVersionUID = 1L;

	@Override
	public String getErrorDescription() {
		
		return "Master APIKey for the user [" + user + "] does not exist";
	}

	@Override
	public String getSuggestedAction() {

		return "Before requesting a short-lived key master key must be generated";
	}

	@Override
	public Integer getHttpStatusCode() {

		return HTTP_ERROR_CODE;
	}

}
