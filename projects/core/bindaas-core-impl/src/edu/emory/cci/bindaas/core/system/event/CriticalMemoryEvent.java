package edu.emory.cci.bindaas.core.system.event;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class CriticalMemoryEvent extends BindaasEvent{

	public CriticalMemoryEvent() {
		super(BindaasEventConstants.CRITICAL_MEMORY);
		
	}
	
	public static class EventData {
		@Expose private String freeMemory;
		@Expose private String totalMemory;
		public String getFreeMemory() {
			return freeMemory;
		}
		public void setFreeMemory(String freeMemory) {
			this.freeMemory = freeMemory;
		}
		public String getTotalMemory() {
			return totalMemory;
		}
		public void setTotalMemory(String totalMemory) {
			this.totalMemory = totalMemory;
		}
		public String getUsedMemoryPercentage() {
			return usedMemoryPercentage;
		}
		public void setUsedMemoryPercentage(String usedMemoryPercentage) {
			this.usedMemoryPercentage = usedMemoryPercentage;
		}
		
		@Expose private String usedMemoryPercentage;
		
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
