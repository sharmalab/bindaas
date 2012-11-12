package edu.emory.cci.bindaas.datasource.provider.genericsql.model;

import com.google.gson.annotations.Expose;

public class DataSourceConfiguration {

	@Expose private String username;
	@Expose private String password;
	@Expose private String url;

	
	public void validate() throws Exception
	{
		if(username == null) throw new Exception("DataSourceConfiguration: username not set");
		if(password == null) throw new Exception("DataSourceConfiguration: password not set");
		if(url == null) throw new Exception("DataSourceConfiguration: url not set");
		
		
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	
}
