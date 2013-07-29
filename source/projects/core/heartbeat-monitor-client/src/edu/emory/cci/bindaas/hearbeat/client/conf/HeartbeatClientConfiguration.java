package edu.emory.cci.bindaas.hearbeat.client.conf;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;

public class HeartbeatClientConfiguration implements ThreadSafe {
	@Expose private String uniqueIdentifier;
	@Expose private boolean enable;
	@Expose private String serverUrl; // URL of the heartbeat server
	@Expose private Integer frequency; // In seconds
	
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public String getServerUrl() {
		return serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	
	
	public Integer getFrequency() {
		return frequency;
	}
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	public Object clone()
	{
		HeartbeatClientConfiguration conf = new HeartbeatClientConfiguration();
		conf.setEnable(this.enable);
		conf.setServerUrl(this.serverUrl);
		conf.setUniqueIdentifier(this.uniqueIdentifier);
		conf.setFrequency(this.frequency);
		return conf;
	}
}
