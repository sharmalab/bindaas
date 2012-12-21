package edu.emory.cci.bindaas.framework.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import edu.emory.cci.bindaas.framework.Activator;

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
	
	private String requestId;
	
	public BindaasEvent(String topic , Map<String,Object> properties) {
		super(topic, properties);
		
		if(requestIdThreadLocal.get() == null)
		{
			requestIdThreadLocal.set(UUID.randomUUID().toString());
		}
		
		this.requestId = requestIdThreadLocal.get();
		
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
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
		
		ServiceReference srf = Activator.getContext().getServiceReference(EventAdmin.class.getName());
		if(srf!=null)
		{
			EventAdmin eventAdmin = (EventAdmin) Activator.getContext().getService(srf);
			return eventAdmin;
		}
		return null;
	}
	
	
}
