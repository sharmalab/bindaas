package edu.emory.cci.bindaas.trusted_app.exception;

import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public class DuplicateJWTException extends AbstractHttpCodeException {

	private String user;
	public final static Integer HTTP_ERROR_CODE = 590;

	public DuplicateJWTException(String user)
	{
		super("edu.emory.cci.bindaas.trusted_app", 1);
		this.user = user;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getErrorDescription() {

		return "JWT for the user [" + user + "] already exists";
	}

	@Override
	public String getSuggestedAction() {

		return "Either use existing token or first delete it and then try generating a new one";
	}

	@Override
	public Integer getHttpStatusCode() {

		return HTTP_ERROR_CODE;
	}
}
