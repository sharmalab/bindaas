package edu.emory.cci.bindaas.core.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.model.Workspace;

public class WorkspaceRequestParameter {
	
	@Expose private JsonObject params;
	@Expose private String description;
	
	public JsonObject getParams() {
		return params;
	}
	public void setParams(JsonObject params) {
		this.params = params;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Workspace getWorkspace(Workspace workspace) throws Exception
	{
		workspace.setParams(getParams());
		workspace.setDescription(getDescription());
		return workspace;
	}
}
