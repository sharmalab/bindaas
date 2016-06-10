package edu.emory.cci.bindaas.commons.mail.api;

public class MailException extends Exception {

	private static final long serialVersionUID = 1L;

	public MailException(String recepient , String message) {
		super( String.format("Message was not sent to [%s] reason=[%s]", recepient , message));
	}

	public MailException(String recepient , Throwable exception) {
		super( String.format("Message was not sent to [%s] reason=[%s]", recepient , exception.getMessage() ) ,exception);

	}

}
