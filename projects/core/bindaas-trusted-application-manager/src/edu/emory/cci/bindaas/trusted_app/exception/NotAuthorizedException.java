package edu.emory.cci.bindaas.trusted_app.exception;

import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public class NotAuthorizedException  extends AbstractHttpCodeException {

	private String detailedMessage;
	public NotAuthorizedException()
	{
		super("edu.emory.cci.bindaas.trusted_app", 1);
	}
	
	public NotAuthorizedException(String detailedMessage) {
		super("edu.emory.cci.bindaas.trusted_app", 1);
		this.detailedMessage = detailedMessage;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getErrorDescription() {
		
		return String.format("%s\t%s", "TrustedApp authentication failed :" , this.detailedMessage == null? "" : this.detailedMessage );
	}

	@Override
	public String getSuggestedAction() {

		return "Please check if correct applicationID and applicationSecret is provided";
	}

	@Override
	public Integer getHttpStatusCode() {

		return 401;
	}

}
