package edu.emory.cci.bindaas.framework.model;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class Profile extends Entity{

	
	@Expose private JsonObject dataSource;
	@Expose private Map<String,QueryEndpoint> queryEndpoints;
	@Expose private Map<String,DeleteEndpoint> deleteEndpoints;
	@Expose private Map<String,SubmitEndpoint> submitEndpoints;
	@Expose private String providerId;
	@Expose private int providerVersion;
	
	
	@Override
	public void validate() throws Exception {
		super.validate();
		
		if(providerId == null || providerId.equals(""))
			throw new Exception("ProviderId not specified");
		if(dataSource == null)
			throw new Exception("DataSource configuration not specified");
	}


	public Profile()
	{
		queryEndpoints = new HashMap<String, QueryEndpoint>();
		deleteEndpoints = new HashMap<String, DeleteEndpoint>();
		submitEndpoints = new HashMap<String, SubmitEndpoint>();
	}


	public JsonObject getDataSource() {
		return dataSource;
	}


	public void setDataSource(JsonObject dataSource) {
		this.dataSource = dataSource;
	}


	public Map<String, QueryEndpoint> getQueryEndpoints() {
		return queryEndpoints;
	}


	public void setQueryEndpoints(Map<String, QueryEndpoint> queryEndpoints) {
		this.queryEndpoints = queryEndpoints;
	}


	public Map<String, DeleteEndpoint> getDeleteEndpoints() {
		return deleteEndpoints;
	}


	public void setDeleteEndpoints(Map<String, DeleteEndpoint> deleteEndpoints) {
		this.deleteEndpoints = deleteEndpoints;
	}


	public Map<String, SubmitEndpoint> getSubmitEndpoints() {
		return submitEndpoints;
	}


	public void setSubmitEndpoints(Map<String, SubmitEndpoint> submitEndpoints) {
		this.submitEndpoints = submitEndpoints;
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
	
}
