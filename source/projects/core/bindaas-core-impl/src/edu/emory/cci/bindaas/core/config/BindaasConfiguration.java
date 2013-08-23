package edu.emory.cci.bindaas.core.config;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class BindaasConfiguration implements ThreadSafe {

	@Expose private String host;
	@Expose private Integer port;
	@Expose private String protocol ;
	@Expose private Boolean enableAuthentication;
	@Expose private Boolean enableAuthorization;
	@Expose private Boolean enableAudit;
	@Expose private String authenticationProviderClass;
	@Expose private String authorizationProviderClass;
	@Expose private String auditProviderClass;
	@Expose private String proxyUrl;
	@Expose private String instanceName;
	
	
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public synchronized String getHost() {
		return host;
	}
	public synchronized void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public synchronized void setPort(Integer port) {
		this.port = port;
	}
	public synchronized String  getProtocol() {
		return protocol;
	}
	public synchronized void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public synchronized Boolean getEnableAuthentication() {
		return enableAuthentication;
	}
	public synchronized void setEnableAuthentication(Boolean enableAuthentication) {
		this.enableAuthentication = enableAuthentication;
	}
	public synchronized Boolean getEnableAuthorization() {
		return enableAuthorization;
	}
	public synchronized void setEnableAuthorization(Boolean enableAuthorization) {
		this.enableAuthorization = enableAuthorization;
	}
	public synchronized Boolean getEnableAudit() {
		return enableAudit;
	}
	public synchronized void setEnableAudit(Boolean enableAudit) {
		this.enableAudit = enableAudit;
	}
	public synchronized String  getAuthenticationProviderClass() {
		return authenticationProviderClass;
	}
	public synchronized void setAuthenticationProviderClass(String authenticationProviderClass) {
		this.authenticationProviderClass = authenticationProviderClass;
	}
	public synchronized String  getAuthorizationProviderClass() {
		return authorizationProviderClass;
	}
	public synchronized void setAuthorizationProviderClass(String authorizationProviderClass) {
		this.authorizationProviderClass = authorizationProviderClass;
	}
	public synchronized String  getAuditProviderClass() {
		return auditProviderClass;
	}
	public synchronized void setAuditProviderClass(String auditProviderClass) {
		this.auditProviderClass = auditProviderClass;
	}
	public synchronized String  getProxyUrl() {
		return proxyUrl;
	}
	public synchronized void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}
	
	public BindaasConfiguration clone()
	{
		JsonElement json = GSONUtil.getGSONInstance().toJsonTree(this);
		return GSONUtil.getGSONInstance().fromJson(json, BindaasConfiguration.class);
		
	}
	@Override
	public void init() throws Exception {
		// do nothing
		
	}
}
