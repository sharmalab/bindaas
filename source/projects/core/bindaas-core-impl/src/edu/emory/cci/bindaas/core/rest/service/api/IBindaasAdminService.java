package edu.emory.cci.bindaas.core.rest.service.api;

public interface IBindaasAdminService {

	public void enableAuthentication() throws Exception;
	public void disableAuthentication() throws Exception;
	
	public void enableMethodLevelAuthorization() throws Exception;
	public void disableMethodLevelAuthorization() throws Exception;
	
	public void setPort(int port) throws Exception;
	public void setHost(String host) throws Exception;
	
	public void restart() throws Exception;
	
	
	public String displayProperties() throws Exception;
	
	public void enableHttps() throws Exception;
	public void disableHttps() throws Exception;
	
	public void enableAudit() throws Exception;
	public void disableAudit() throws Exception;
	
	public String showStatus();
}
