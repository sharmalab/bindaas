package edu.emory.cci.bindaas.framework.event;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.bundle.Activator;

public class BindaasEvent extends Event{

	private static Set<String> topicsEnabled;
	private static final ThreadLocal<String> requestIdThreadLocal = new ThreadLocal<String>();
	
	static {
		topicsEnabled = new HashSet<String>();
	}
	
	public static void addTopic(String topic)
	{
		topicsEnabled.add(topic);
	}
	
	public static void removeTopic(String topic)
	{
		topicsEnabled.remove(topic);
	}
	
	private Date timestamp;
	
	// identifies the requesting thread. set automatically
	private String threadId;
	
	// provide a reference for this event.It can be used to associate multiple events,log messages, etc.
	private String referenceId;
	
	// information about the event
	private JsonObject eventData;
	
	
	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public JsonObject getEventData() {
		return eventData;
	}

	public void setEventData(JsonObject eventData) {
		this.eventData = eventData;
	}

	public BindaasEvent(String topic)
	{
		this(topic , new HashMap<String, Object>());
		timestamp = new Date();
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public BindaasEvent(String topic , Map<String,Object> properties) {
		super(topic, properties);
		
		if(requestIdThreadLocal.get() == null)
		{
			requestIdThreadLocal.set(UUID.randomUUID().toString());
		}
		
		this.threadId = requestIdThreadLocal.get();
		
	}

	
	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public void emitSynchronously()
	{
		if( topicsEnabled.contains(getTopic()) )
		{
			EventAdmin eventAdmin = getEventAdmin();
			if(eventAdmin!=null)
				eventAdmin.sendEvent(this);
		}
		
	}
	
	public void emitAsynchronously()
	{
		if( topicsEnabled.contains(getTopic()) )
		{
			EventAdmin eventAdmin = getEventAdmin();
			if(eventAdmin!=null)
				eventAdmin.postEvent(this);	
		}
		
	}
	
	public EventAdmin getEventAdmin()
	{
		
		@SuppressWarnings("rawtypes")
		ServiceReference srf = Activator.getContext().getServiceReference(EventAdmin.class.getName());
		if(srf!=null)
		{
			@SuppressWarnings("unchecked")
			EventAdmin eventAdmin = (EventAdmin) Activator.getContext().getService(srf);
			return eventAdmin;
		}
		return null;
	}
	
	
}
