package edu.emory.cci.bindaas.security_dashboard.config;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;

public class SecurityDashboardConfiguration implements ThreadSafe{
	@Expose private String rakshakBaseUrl;
	/**
	 * apiKeyGroup is a special group that is associated with those users who have an apiKey assigned to them.
	 * when system boots, all users from the local-db are cross-referenced with users in rakshak. if there is a match and they are not a member of this group,
	 * they are automatically added to it - synchronizeAtStart method
	 * 
	 * 
	 * 
	 */
	
	@Expose private String apiKeyGroup;
	@Expose private boolean syncOnStartup;

	@Override
	public void init() throws Exception {
	
		
	}
	
	public Object clone()
	{
		SecurityDashboardConfiguration newObj = new SecurityDashboardConfiguration();
		newObj.rakshakBaseUrl = this.rakshakBaseUrl;
		return newObj;
	}

	public String getRakshakBaseUrl() {
		return rakshakBaseUrl;
	}

	public void setRakshakBaseUrl(String rakshakBaseUrl) {
		this.rakshakBaseUrl = rakshakBaseUrl;
	}

	public String getApiKeyGroup() {
		return apiKeyGroup;
	}

	public void setApiKeyGroup(String apiKeyGroup) {
		this.apiKeyGroup = apiKeyGroup;
	}

	public boolean isSyncOnStartup() {
		return syncOnStartup;
	}

	public void setSyncOnStartup(boolean syncOnStartup) {
		this.syncOnStartup = syncOnStartup;
	}
	
	
	
}

