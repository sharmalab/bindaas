package edu.emory.cci.bindaas.core.model;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.model.BindVariable;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;


public class QueryEndpointRequestParameter {

	@Expose private String queryTemplate;
	@Expose private String description;
	@Expose private JsonObject metaData;
	@Expose private List<String> tags;
	@Expose private Map<String,BindVariable> bindVariables;
	@Expose private JsonObject outputFormat;
	@Expose private Map<Integer,ModifierEntry> queryModifiers;
	@Expose private Map<Integer,ModifierEntry> queryResultModifiers;
	
	
	public String getQueryTemplate() {
		return queryTemplate;
	}
	public void setQueryTemplate(String queryTemplate) {
		this.queryTemplate = queryTemplate;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public JsonObject getMetaData() {
		return metaData;
	}
	public void setMetaData(JsonObject metaData) {
		this.metaData = metaData;
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
	public JsonObject getOutputFormat() {
		return outputFormat;
	}
	public void setOutputFormat(JsonObject outputFormat) {
		this.outputFormat = outputFormat;
	}
	public Map<Integer, ModifierEntry> getQueryModifiers() {
		return queryModifiers;
	}
	public void setQueryModifiers(Map<Integer, ModifierEntry> queryModifiers) {
		this.queryModifiers = queryModifiers;
	}
	public Map<Integer, ModifierEntry> getQueryResultModifiers() {
		return queryResultModifiers;
	}
	public void setQueryResultModifiers(
			Map<Integer, ModifierEntry> queryResultModifiers) {
		this.queryResultModifiers = queryResultModifiers;
	}
	
}
