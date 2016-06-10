package edu.emory.cci.bindaas.datasource.provider.mongodb.model;

import com.google.gson.annotations.Expose;

public class DataSourceConfiguration {

	@Expose private String db;
	@Expose private String collection;
	@Expose private String host;
	@Expose private int port = 27017;
	@Expose private boolean initialize;
	
	
	public boolean isInitialize() {
		return initialize;
	}

	public void setInitialize(boolean initialize) {
		this.initialize = initialize;
	}

	private void check(String propName,String propValue) throws Exception
	{
		if(propValue == null) throw new Exception("DataSourceConfiguration: " + propName + " not set");
	}
	
	public void validate() throws Exception
	{
		check("db" , db);
		check("collection" , collection);
		check("host", host);
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}	
	
	
}
