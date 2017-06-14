package edu.emory.cci.bindaas.core.system.event;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class DeadlockDetectedEvent extends BindaasEvent{

	public DeadlockDetectedEvent()
	{
		super(BindaasEventConstants.DEADLOCK_DETECTED);
	}
	
	public static class EventData {
		@Expose private String[] stackTraces;

		public String[] getStackTraces() {
			return stackTraces;
		}

		public void setStackTraces(StackTraceElement[] stackTraces) {
			String[] strings = new String[stackTraces.length];
			for(int i = 0 ; i < stackTraces.length ; i++ )
			{
				strings[i] = stackTraces[i].toString();
			}
			
			this.stackTraces = strings;
		}
		
	}
	
	private EventData data;

	public EventData getData() {
		return data;
	}

	public void setData(EventData data) {
		this.data = data;
		JsonObject eventData  = GSONUtil.getGSONInstance().toJsonTree(data).getAsJsonObject();
		setEventData(eventData);
	}
	
}
