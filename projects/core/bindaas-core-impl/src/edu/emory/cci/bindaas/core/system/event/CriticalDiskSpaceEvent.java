package edu.emory.cci.bindaas.core.system.event;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class CriticalDiskSpaceEvent extends BindaasEvent {
	
	public CriticalDiskSpaceEvent() {
		super(BindaasEventConstants.CRITICAL_DISK_SPACE);
		
	}
		
	public static class EventData {
		@Expose private String freeSpace;
		@Expose private String totalSpace;
		@Expose private String usagebleSpace;
		@Expose private String rootPath;
		@Expose private String utilization;
		
		
		public String getUtilization() {
			return utilization;
		}
		public void setUtilization(String utilization) {
			this.utilization = utilization;
		}
		public String getFreeSpace() {
			return freeSpace;
		}
		public void setFreeSpace(String freeSpace) {
			this.freeSpace = freeSpace;
		}
		public String getTotalSpace() {
			return totalSpace;
		}
		public void setTotalSpace(String totalSpace) {
			this.totalSpace = totalSpace;
		}
		public String getUsagebleSpace() {
			return usagebleSpace;
		}
		public void setUsagebleSpace(String usagebleSpace) {
			this.usagebleSpace = usagebleSpace;
		}
		public String getRootPath() {
			return rootPath;
		}
		public void setRootPath(String rootPath) {
			this.rootPath = rootPath;
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
