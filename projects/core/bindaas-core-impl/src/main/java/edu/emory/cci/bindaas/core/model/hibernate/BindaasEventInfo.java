package edu.emory.cci.bindaas.core.model.hibernate;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import edu.emory.cci.bindaas.framework.event.BindaasEvent;

@Entity
public class BindaasEventInfo {

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
	private String threadId;
	private String referenceId;
	private String eventData;
	private String topic;
	private String eventType;

	
	public BindaasEventInfo(){}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getEventData() {
		return eventData;
	}

	public void setEventData(String eventData) {
		this.eventData = eventData;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public BindaasEventInfo(BindaasEvent bindaasEvent) {

		this.timestamp = bindaasEvent.getTimestamp();
		this.eventType = bindaasEvent.getClass().getSimpleName();
		this.referenceId = bindaasEvent.getReferenceId();
		this.threadId = bindaasEvent.getThreadId();
		this.topic = bindaasEvent.getTopic();
		this.eventData = bindaasEvent.getEventData().toString();
	}
}
