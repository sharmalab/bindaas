package edu.emory.cci.bindaas.framework.provider.exception;

public class NetworkConnectionException extends AbstractHttpCodeException{
	private static final long serialVersionUID = 1L;
	public NetworkConnectionException(String providerId,
			Integer providerVersion, String arg0, Throwable arg1) {
		super(providerId, providerVersion, arg0, arg1);
		
	}

	public NetworkConnectionException(String providerId,
			Integer providerVersion, String arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public NetworkConnectionException(String providerId,
			Integer providerVersion, Throwable arg0) {
		super(providerId, providerVersion, arg0);
		
	}

	public NetworkConnectionException(String providerId, Integer providerVersion) {
		super(providerId, providerVersion);
		
	}

	@Override
	public Integer getHttpStatusCode() {
		
		return AbstractHttpCodeException.NETWORK_CONNECTION_ERROR;
	}

	@Override
	public String getErrorDescription() {
		return "Error establishing connection with the remote server/database";
	}

	@Override
	public String getSuggestedAction() {

		return "Check server ip,username,password and other information";
	}

}
