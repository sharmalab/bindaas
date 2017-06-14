package edu.emory.cci.bindaas.commons.mail.api;

import java.util.List;

public interface IMailService {

	public void sendMail(String recepient , String subject ,  String message) throws MailException;
	public void sendMail(List<String> recepients , String subject , String message) throws MailException;
	
}
