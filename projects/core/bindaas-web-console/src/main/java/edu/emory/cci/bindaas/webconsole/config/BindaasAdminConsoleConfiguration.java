package edu.emory.cci.bindaas.webconsole.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.ThreadSafe;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class BindaasAdminConsoleConfiguration implements ThreadSafe{
	@Expose private String host;
	@Expose private Integer port;
	@Expose private String proxyUrl;
	@Expose private Set<String> adminAccounts;
	
	
	public synchronized String  getHost() {
		return host;
	}

	public synchronized void setHost(String host) {
		this.host = host;
	}

	public synchronized Integer  getPort() {
		return port;
	}

	public synchronized void setPort(Integer port) {
		this.port = port;
		
	}

	public synchronized String  getProxyUrl() {
		return proxyUrl;
	}

	public synchronized void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}

	public Set<String> getAdminAccounts() {
		return adminAccounts;
	}

	public synchronized void setAdminAccounts(Set<String> adminAccounts) {
		this.adminAccounts = adminAccounts;
	}

	public synchronized Boolean getEnableProxy() {
		return enableProxy;
	}

	public synchronized void setEnableProxy(Boolean enableProxy) {
		this.enableProxy = enableProxy;
	}

	public AdminConfiguration getAdminConfiguration() {
		return adminConfiguration;
	}

	public synchronized void setAdminConfiguration(AdminConfiguration adminConfiguration) {
		this.adminConfiguration = adminConfiguration;
	}

	public UserConfiguration getUserConfiguration() {
		return userConfiguration;
	}

	public synchronized void setUserConfiguration(UserConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}

	@Expose private Boolean enableProxy;
	@Expose private AdminConfiguration adminConfiguration;
	@Expose private UserConfiguration userConfiguration;
	@Expose private UserAccountManagement userAccountManagement;
	
	public synchronized  UserAccountManagement getUserAccountManagement() {
		return userAccountManagement;
	}

	public  synchronized void setUserAccountManagement(UserAccountManagement userAccountManagement) {
		this.userAccountManagement = userAccountManagement;
	}

	public static class AdminConfiguration {
		@Expose private AuthenticationMethod authenticationMethod;
		@Expose private Map<String,Boolean> openIdProviders;
		@Expose private String ldapUrl;
		@Expose private String ldapDNPattern;
		
		public AuthenticationMethod getAuthenticationMethod() {
			return authenticationMethod;
		}

		public synchronized void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
		}

		public Map<String, Boolean> getOpenIdProviders() {
			return openIdProviders;
		}

		public synchronized void setOpenIdProviders(Map<String, Boolean> openIdProviders) {
			this.openIdProviders = openIdProviders;
		}

		public synchronized String  getLdapUrl() {
			return ldapUrl;
		}

		public synchronized void setLdapUrl(String ldapUrl) {
			this.ldapUrl = ldapUrl;
		}

		public synchronized String  getLdapDNPattern() {
			return ldapDNPattern;
		}

		public synchronized void setLdapDNPattern(String ldapDNPattern) {
			this.ldapDNPattern = ldapDNPattern;
		}

		public static enum AuthenticationMethod{
			ldap,openid,defaultMethod
		}
	}

	public static class UserConfiguration {
		@Expose private AuthenticationMethod authenticationMethod;
		@Expose private Map<String,Boolean> openIdProviders;
		@Expose private String ldapUrl;
		
		public AuthenticationMethod getAuthenticationMethod() {
			return authenticationMethod;
		}

		public synchronized void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
		}

		public Map<String, Boolean> getOpenIdProviders() {
			return openIdProviders;
		}

		public synchronized void setOpenIdProviders(Map<String, Boolean> openIdProviders) {
			this.openIdProviders = openIdProviders;
		}

		public synchronized String  getLdapUrl() {
			return ldapUrl;
		}

		public synchronized void setLdapUrl(String ldapUrl) {
			this.ldapUrl = ldapUrl;
		}

		public synchronized String  getLdapDNPattern() {
			return ldapDNPattern;
		}

		public synchronized void setLdapDNPattern(String ldapDNPattern) {
			this.ldapDNPattern = ldapDNPattern;
		}

		@Expose private String ldapDNPattern;
		
		public static enum AuthenticationMethod{
			ldap,openid,none
		}
	}

	public static class UserAccountManagement
	{
		@Expose private Boolean enableUserSignupNotification;
		@Expose private List<String> notificationRecepients;
		
		public Boolean getEnableUserSignupNotification() {
			return enableUserSignupNotification;
		}
		public synchronized void setEnableUserSignupNotification(Boolean enableUserSignupNotification) {
			this.enableUserSignupNotification = enableUserSignupNotification;
		}
		public synchronized List<String> getNotificationRecepients() {
			return notificationRecepients;
		}
		public synchronized void setNotificationRecepients(List<String> notificationRecepients) {
			this.notificationRecepients = notificationRecepients;
		}
		
	}
	
	public BindaasAdminConsoleConfiguration clone()
	{
		JsonElement json = GSONUtil.getGSONInstance().toJsonTree(this);
		return GSONUtil.getGSONInstance().fromJson(json, BindaasAdminConsoleConfiguration.class);
		
	}

	@Override
	public void init() throws Exception {
		// do nothing
	}
}

