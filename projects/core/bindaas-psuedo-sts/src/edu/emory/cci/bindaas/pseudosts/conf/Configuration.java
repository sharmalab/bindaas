package edu.emory.cci.bindaas.pseudosts.conf;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;

public class Configuration implements ThreadSafe{

	@Expose private String ldapProviderClass;
	@Expose private  String defaultClientId = "external.org";
	@Expose private Integer defaultLifespanOfKeysInSeconds = 3600;
	
	public String getDefaultClientId() {
		return defaultClientId;
	}

	public void setDefaultClientId(String defaultClientId) {
		this.defaultClientId = defaultClientId;
	}

	public Integer getDefaultLifespanOfKeysInSeconds() {
		return defaultLifespanOfKeysInSeconds;
	}

	public void setDefaultLifespanOfKeysInSeconds(
			Integer defaultLifespanOfKeysInSeconds) {
		this.defaultLifespanOfKeysInSeconds = defaultLifespanOfKeysInSeconds;
	}

	public String getLdapProviderClass() {
		return ldapProviderClass;
	}

	public void setLdapProviderClass(String ldapProviderClass) {
		this.ldapProviderClass = ldapProviderClass;
	}

	public Object clone()
	{
		Configuration newConfig = new Configuration();
		newConfig.ldapProviderClass = ldapProviderClass;
		newConfig.defaultClientId = defaultClientId;
		newConfig.defaultLifespanOfKeysInSeconds = defaultLifespanOfKeysInSeconds;
		return newConfig;
	}
	
	@Override
	public void init() throws Exception {

		
	}
	
}
