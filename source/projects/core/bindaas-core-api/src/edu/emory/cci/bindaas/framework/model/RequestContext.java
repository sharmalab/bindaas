package edu.emory.cci.bindaas.framework.model;

import java.util.Map;

public class RequestContext {
	private String user;
	private Map<String,Object> attributes;

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
