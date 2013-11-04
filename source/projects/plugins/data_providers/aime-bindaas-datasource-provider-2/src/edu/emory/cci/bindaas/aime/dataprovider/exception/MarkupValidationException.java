package edu.emory.cci.bindaas.aime.dataprovider.exception;

public class MarkupValidationException extends Exception{
	
	public MarkupValidationException(String message , Exception e) {
		super(message , e);
	}
	
	public MarkupValidationException(String message){
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
