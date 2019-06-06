package edu.emory.cci.bindaas.trusted_app_client.app.exception;

public class ClientException extends Exception {

	
	private static final long serialVersionUID = 1L;
	
	
	public ClientException(Throwable error)
	{
		super(error);
		
	}
	
	public ClientException(String message , Throwable error)
	{
		super(message , error);
		
	}
	
}
