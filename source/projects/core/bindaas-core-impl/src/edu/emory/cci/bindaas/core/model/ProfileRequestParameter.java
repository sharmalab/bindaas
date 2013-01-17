package edu.emory.cci.bindaas.core.model;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;

public class ProfileRequestParameter {
	
	@Expose private JsonObject dataSource;
	@Expose private String providerId;
	@Expose private int providerVersion;
	@Expose private String description;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public JsonObject getDataSource() {
		return dataSource;
	}
	public void setDataSource(JsonObject dataSource) {
		this.dataSource = dataSource;
	}
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public int getProviderVersion() {
		return providerVersion;
	}
	public void setProviderVersion(int providerVersion) {
		this.providerVersion = providerVersion;
	}

	public Profile getProfile(Profile profile) throws Exception
	{
		profile.setDataSource(getDataSource());
		profile.setProviderId(getProviderId());
		profile.setProviderVersion(getProviderVersion());
		profile.setDescription(getDescription());
		
		return profile;
	}
}
