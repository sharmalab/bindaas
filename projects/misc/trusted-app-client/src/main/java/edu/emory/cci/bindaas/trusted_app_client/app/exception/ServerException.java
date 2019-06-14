package edu.emory.cci.bindaas.trusted_app_client.app.exception;

public class ServerException extends Exception  {

	
	private static final long serialVersionUID = 1L;
	private Integer errorCode;
	
	public ServerException(Integer errorCode , String message)
	{
		super(message);
		this.errorCode = errorCode;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}
	
	
	
}
