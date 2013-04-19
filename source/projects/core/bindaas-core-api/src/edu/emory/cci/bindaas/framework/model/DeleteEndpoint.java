package edu.emory.cci.bindaas.framework.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class DeleteEndpoint extends Entity{

	@Expose private String queryTemplate;
	@Expose private List<String> tags;
	@Expose private Map<String,BindVariable> bindVariables;
	@Expose private Stage stage;
	
	
	@Override
	public void validate() throws Exception {
		super.validate();
		
		if(queryTemplate == null)
			throw new Exception("QueryTemplate not specified");
		
		if(tags == null)
			tags = new ArrayList<String>();
		
		if(bindVariables == null)
			bindVariables = new HashMap<String, BindVariable>();
	
		
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
	public Stage getStage() {
		return stage;
	}
	public void setStage(Stage stage) {
		this.stage = stage;
	}
}
