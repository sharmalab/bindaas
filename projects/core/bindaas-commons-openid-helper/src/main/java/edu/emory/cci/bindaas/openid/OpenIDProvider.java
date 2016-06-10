package edu.emory.cci.bindaas.openid;

import java.util.Properties;

public class OpenIDProvider {

	private String uri;
	private Properties attributes;
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Properties getAttributes() {
		return attributes;
	}
	public void setAttributes(Properties attributes) {
		this.attributes = attributes;
	}
	
}
