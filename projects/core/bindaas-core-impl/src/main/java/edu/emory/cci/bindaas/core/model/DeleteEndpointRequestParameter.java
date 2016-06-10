package edu.emory.cci.bindaas.core.model;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.model.BindVariable;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;

public class DeleteEndpointRequestParameter {
	@Expose private String queryTemplate;
	@Expose private List<String> tags;
	@Expose private Map<String,BindVariable> bindVariables;
	@Expose private String description;
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getQueryTemplate() {
		return queryTemplate;
	}
	public void setQueryTemplate(String queryTemplate) {
		this.queryTemplate = queryTemplate;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public Map<String, BindVariable> getBindVariables() {
		return bindVariables;
	}
	public void setBindVariables(Map<String, BindVariable> bindVariables) {
		this.bindVariables = bindVariables;
	}
	
	
	public DeleteEndpoint getDeleteEndpoint(DeleteEndpoint deleteEndpoint) throws Exception
	{		
		deleteEndpoint.setBindVariables(getBindVariables());
		deleteEndpoint.setQueryTemplate(getQueryTemplate());
		deleteEndpoint.setTags(getTags());
		deleteEndpoint.setDescription(getDescription());
		
		return deleteEndpoint;
	}
	public static DeleteEndpointRequestParameter convert(
			DeleteEndpoint deleteEndpoint) {
		DeleteEndpointRequestParameter derp = new DeleteEndpointRequestParameter();
		derp.setBindVariables(deleteEndpoint.getBindVariables());
		derp.setQueryTemplate(deleteEndpoint.getQueryTemplate());
		derp.setDescription(deleteEndpoint.getDescription());
		derp.setTags(deleteEndpoint.getTags());
		return derp;
	}
}
