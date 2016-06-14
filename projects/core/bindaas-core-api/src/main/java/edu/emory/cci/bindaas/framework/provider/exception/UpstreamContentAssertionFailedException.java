package edu.emory.cci.bindaas.framework.provider.exception;
/**
 * To be thrown by Query Result Modifiers when some assertion made about the upstream content fails.
 * Such situations may arise due to error made by API creator in using incompatible QRM and Queries.
 * @author nadir
 *
 */
public class UpstreamContentAssertionFailedException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	public UpstreamContentAssertionFailedException(String providerId,
			Integer providerVersion, String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
		
	}

	public UpstreamContentAssertionFailedException(String providerId,
			Integer providerVersion, String arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public UpstreamContentAssertionFailedException(String providerId,
			Integer providerVersion, Throwable arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public UpstreamContentAssertionFailedException(String providerId,
			Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	@Override
	public Integer getHttpStatusCode() {
		return AbstractHttpCodeException.UPSTREAM_CONTENT_ASSERTION_FAILED;
	}
	
	@Override
	public String getErrorDescription() {
		return "Assertion made by Query Result Modifier regarding upstream data failed.\nUsually this points to an error made by API creator";
	}

	@Override
	public String getSuggestedAction() {
		return "Check the API and see if the results returned by the Query are compatible with Query Result Modifier";
	}


}
