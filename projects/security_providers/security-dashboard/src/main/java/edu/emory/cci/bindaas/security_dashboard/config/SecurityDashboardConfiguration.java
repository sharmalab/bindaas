package edu.emory.cci.bindaas.security_dashboard.config;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;

public class SecurityDashboardConfiguration implements ThreadSafe{
	@Expose private String rakshakBaseUrl;
	
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
	
	
}

