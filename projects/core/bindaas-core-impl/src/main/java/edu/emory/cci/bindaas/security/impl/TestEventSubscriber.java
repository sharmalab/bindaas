package edu.emory.cci.bindaas.security.impl;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;

public class TestEventSubscriber implements EventHandler {
	
	public TestEventSubscriber()
	{
		BindaasEvent.addTopic(BindaasEventConstants.CREATE_PROFILE_TOPIC);
	}

	@Override
	public void handleEvent(Event event) {
		if(event instanceof BindaasEvent)
		{
			BindaasEvent be = (BindaasEvent) event;
			
		}
		
	}

}
