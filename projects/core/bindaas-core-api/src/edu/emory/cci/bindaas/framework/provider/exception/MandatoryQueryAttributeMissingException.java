package edu.emory.cci.bindaas.framework.provider.exception;

public class MandatoryQueryAttributeMissingException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	@Override
	public Integer getHttpStatusCode() {
		
		return AbstractHttpCodeException.MISSING_MANDATORY_QUERY_PARAMETER_ERROR;
	}

	public MandatoryQueryAttributeMissingException(
			String arg0) {
		super("system", 1, String.format("Mandatory Attribute Missing : [%s]", arg0));
	}

	@Override
	public String getErrorDescription() {
		return "This error indicates one or many mandatory query parameters are missing";
	}

	@Override
	public String getSuggestedAction() {
		return "Check the API documentation to verify if all mandatory attributes were provided";
	}
	

}
