package edu.emory.cci.bindaas.datasource.provider.aime.model;

import com.google.gson.annotations.Expose;

public class DataSourceConfiguration {

	@Expose private String username;
	@Expose private String password;
	@Expose private String url;
	@Expose private boolean autoInitialize;
	@Expose private String tableName;
	
	
	public void validate() throws Exception
	{
		if(username == null) throw new Exception("DataSourceConfiguration: username not set");
		if(password == null) throw new Exception("DataSourceConfiguration: password not set");
		if(url == null) throw new Exception("DataSourceConfiguration: url not set");
		if(autoInitialize == true)
		{
			if(tableName == null) throw new Exception("DataSourceConfiguration: tableName not set");
		}
		
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
	public boolean isAutoInitialize() {
		return autoInitialize;
	}
	public void setAutoInitialize(boolean autoInitialize) {
		this.autoInitialize = autoInitialize;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
	
}
