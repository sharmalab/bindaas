package edu.emory.cci.bindaas.framework.model;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class Workspace extends Entity
{

	 @Expose private Map<String,Profile> profiles;
	 @Expose private JsonObject params;
	
	 
	 public void validate() throws Exception
	 {
		 super.validate();
		 
		 if(params == null)
			 params = new JsonObject();
	 }
	public JsonObject getParams() {
		return params;
	}

	public void setParams(JsonObject params) {
		this.params = params;
	}

	public Workspace()
	{
		profiles = new HashMap<String, Profile>();
	}

	public Map<String, Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(Map<String, Profile> profiles) {
		this.profiles = profiles;
	}
	
}
