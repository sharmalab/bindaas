package edu.emory.cci.bindaas.core.model.hibernate;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class HistoryLog {
	public static enum ActivityType{
		
		SYSTEM_APPROVE ("system-approve") , APPROVE("approve") , REFRESH("refresh") , REVOKE("revoke") , DENY("deny") ;
		
		private String value;
		
		ActivityType(String value)
		{
			this.value = value;
		}
		
		public String toString(){
			return value;
		}
		
	}

	public HistoryLog()
	{
		this.activityDate = new Date();
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private Long id;
	
	@ManyToOne(optional = false)
	private UserRequest userRequest;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date activityDate;
	
	private String comments;
	private String initiatedBy;
	private String activityType;
	
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public UserRequest getUserRequest() {
		return userRequest;
	}
	public void setUserRequest(UserRequest userRequest) {
		this.userRequest = userRequest;
	}
	public Date getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getInitiatedBy() {
		return initiatedBy;
	}
	public void setInitiatedBy(String initiatedBy) {
		this.initiatedBy = initiatedBy;
	}
	
	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType.toString();
	}
	
}
