package edu.emory.cci.bindaas.commons.mail.impl;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.emory.cci.bindaas.commons.mail.api.IMailService;
import edu.emory.cci.bindaas.commons.mail.api.MailException;
import edu.emory.cci.bindaas.commons.mail.bundle.Activator;
import edu.emory.cci.bindaas.core.util.DynamicProperties;

public class MailServiceImpl implements IMailService {

	private Properties defaultProperties;
	private DynamicProperties dynamicProperties;
	public DynamicProperties getDynamicProperties() {
		return dynamicProperties;
	}

	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	public void setDynamicProperties(DynamicProperties dynamicProperties) {
		this.dynamicProperties = dynamicProperties;
	}

	private String username;

	private Session session;

	public void init() throws Exception {
		
		dynamicProperties = new DynamicProperties("mailService", defaultProperties , Activator.getContext());
		this.username = (String) dynamicProperties.get("username");
		final String password = (String) dynamicProperties.get("password");
		
		
		session = Session.getInstance( dynamicProperties.getProperties() , new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		
		
		// send test mail
		
//		sendMail("nadirsaghar@gmail.com", "Test", "Test Message");
	}

	@Override
	public void sendMail(String recepient, String subject, String message)
			throws MailException {
		try {
			Message emessage = new MimeMessage(session);
			emessage.setFrom(new InternetAddress(username));
			emessage.setRecipients(Message.RecipientType.TO,InternetAddress.parse(recepient));
			emessage.setSubject(subject);
			emessage.setText(message);
			Transport.send(emessage);
		} catch (Exception e) {
			throw new MailException(recepient, e);
		}

	}


	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public void sendMail(List<String> recepients, String subject, String message)
			throws MailException {
		
		for(String recepient : recepients)
		{
			sendMail(recepient, subject, message);
		}


	}

}
