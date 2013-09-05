package edu.emory.cci.bindaas.core.apikey.api;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class APIKey {
	@Expose private String value;
	@Expose private Date expires;
	@Expose private String username;
	@Expose private String firstName;
	@Expose private String lastName;
	@Expose private String emailAddress;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	
}
