package edu.emory.cci.bindaas.hearbeat.mongo;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;

public class MongoHeartbeatLoggerConfiguration implements ThreadSafe {
	@Expose private boolean enable;
	@Expose private String host;
	@Expose private Integer port;
	@Expose private String db;
	@Expose private String collection;
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
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
	
	public Object clone()
	{
		MongoHeartbeatLoggerConfiguration conf = new MongoHeartbeatLoggerConfiguration();
		conf.setCollection(this.collection);
		conf.setHost(this.host);
		conf.setPort(this.port);
		conf.setDb(this.db);
		conf.setEnable(this.enable);
		return conf;
	}
	
	@Override
	public void init() throws Exception {		
	}
	
}
