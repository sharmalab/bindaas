package edu.emory.cci.bindaas.core.model.hibernate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import com.google.gson.annotations.Expose;

@Entity
public class UserRequest {

	public enum Stage {
		pending,accepted,revoked,denied
	}
	
	public UserRequest()
	{
		requestDate = new Date();
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private Long id;
	
	@Expose
	private String emailAddress;
	
	@Expose
	private String firstName;
	@Expose
	private String lastName;
	@Expose
	private String reason;
	
	@Expose
	@Temporal(TemporalType.TIMESTAMP)
	private Date requestDate;
	
	@Expose
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateExpires;
	
	@Expose
	private String apiKey;
	
	@Expose
	@Column(nullable = false)
	private String stage; // pending|accepted|revoked|denied
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
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
	public String getReason() {
		return reason;
	}
	
	public void setStage(Stage stage)
	{
		this.stage = stage.name();
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	
	public Date getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
	
	public Date getDateExpires() {
		return dateExpires;
	}
	public void setDateExpires(Date dateExpires) {
		this.dateExpires = dateExpires;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getStage() {
		return stage;
	}
	public void setStage(String stage) {
		this.stage = stage;
	}

}
