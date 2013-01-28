package edu.emory.cci.bindaas.webconsole.admin;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.admin.EmailConfigurationPanelAction.Request;

public class SecurityAction implements IAdminAction{

	private String actionName;
	private Log log = LogFactory.getLog(getClass());
	
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public String getActionName() {
		return actionName;
	}


	@Override
	public String doAction(JsonObject payload, HttpServletRequest request)
			throws Exception {
		Request requestObject = GSONUtil.getGSONInstance().fromJson(payload, Request.class);
		DynamicProperties ldapServiceProps = Activator.getService(DynamicProperties.class , "(name=bindaas.authentication.ldap)");
		DynamicProperties bindaasProperties = Activator.getService(DynamicProperties.class , "(name=bindaas)");
		requestObject.saveToDynamicProperties(bindaasProperties , ldapServiceProps);
		return "success";
	}
	
	public static class Request {
		
		@Expose String webConsoleSecurityMethod;
		@Expose String webConsoleLDAPServer;
		@Expose String webConsoleDNPattern;
		public String getWebConsoleSecurityMethod() {
			return webConsoleSecurityMethod;
		}

		public void setWebConsoleSecurityMethod(String webConsoleSecurityMethod) {
			this.webConsoleSecurityMethod = webConsoleSecurityMethod;
		}

		public String getWebConsoleLDAPServer() {
			return webConsoleLDAPServer;
		}

		public void setWebConsoleLDAPServer(String webConsoleLDAPServer) {
			this.webConsoleLDAPServer = webConsoleLDAPServer;
		}

		public String getWebConsoleDNPattern() {
			return webConsoleDNPattern;
		}

		public void setWebConsoleDNPattern(String webConsoleDNPattern) {
			this.webConsoleDNPattern = webConsoleDNPattern;
		}

		public String getWebConsoleAdminAccounts() {
			return webConsoleAdminAccounts;
		}

		public void setWebConsoleAdminAccounts(String webConsoleAdminAccounts) {
			this.webConsoleAdminAccounts = webConsoleAdminAccounts;
		}

		public Boolean getWebConsoleEnableNotification() {
			return webConsoleEnableNotification;
		}

		public void setWebConsoleEnableNotification(Boolean webConsoleEnableNotification) {
			this.webConsoleEnableNotification = webConsoleEnableNotification;
		}

		public String getWebConsoleSendNotificationTo() {
			return webConsoleSendNotificationTo;
		}

		public void setWebConsoleSendNotificationTo(String webConsoleSendNotificationTo) {
			this.webConsoleSendNotificationTo = webConsoleSendNotificationTo;
		}

		public Boolean getMiddlewareEnableAuthentication() {
			return middlewareEnableAuthentication;
		}

		public void setMiddlewareEnableAuthentication(
				Boolean middlewareEnableAuthentication) {
			this.middlewareEnableAuthentication = middlewareEnableAuthentication;
		}

		@Expose String webConsoleAdminAccounts;
		@Expose Boolean webConsoleEnableNotification;
		@Expose String webConsoleSendNotificationTo;
		@Expose Boolean middlewareEnableAuthentication;
		
		public static Request fromDynamicProperties(DynamicProperties bindaasProperties , DynamicProperties ldapServiceProperties)
		{
			Request request = new Request();
			request.webConsoleSecurityMethod = (String) bindaasProperties.get("webconsole.security.method");
			request.webConsoleLDAPServer = (String) ldapServiceProperties.get("ldap.url");
			request.webConsoleDNPattern = (String) ldapServiceProperties.get("ldap.dn.pattern");
			request.webConsoleAdminAccounts = (String) bindaasProperties.get("webconsole.security.admin");
			request.webConsoleEnableNotification = bindaasProperties.get("webconsole.security.userRegistration.notification.enable")!=null ? Boolean.parseBoolean(bindaasProperties.get("webconsole.security.userRegistration.notification.enable")) : null;
			request.webConsoleSendNotificationTo = (String) bindaasProperties.get("webconsole.security.userRegistration.notification.recepient");
			request.middlewareEnableAuthentication = bindaasProperties.get("authentication.status")!=null ? Boolean.parseBoolean(bindaasProperties.get("authentication.status")) : null;
			
			return request;
			
		}
		
		public void saveToDynamicProperties(DynamicProperties bindaasProperties , DynamicProperties ldapServiceProperties)
		{
			bindaasProperties.put("webconsole.security.method", webConsoleSecurityMethod);
			bindaasProperties.put("webconsole.security.admin", webConsoleAdminAccounts);
			bindaasProperties.put("webconsole.security.userRegistration.notification.enable", webConsoleEnableNotification.toString());
			bindaasProperties.put("webconsole.security.userRegistration.notification.recepient", webConsoleSendNotificationTo);
			bindaasProperties.put("authentication.status", middlewareEnableAuthentication.toString() );
			
			ldapServiceProperties.put("ldap.url", webConsoleLDAPServer);
			ldapServiceProperties.put("ldap.dn.pattern", webConsoleDNPattern);
			
			
		}
		
	}

}
