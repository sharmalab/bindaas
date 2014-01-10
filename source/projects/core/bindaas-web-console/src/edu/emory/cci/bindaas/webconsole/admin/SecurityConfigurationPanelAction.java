package edu.emory.cci.bindaas.webconsole.admin;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration.AdminConfiguration;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration.UserConfiguration;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration.UserConfiguration.AuthenticationMethod;

public class SecurityConfigurationPanelAction implements IAdminAction{

	private String actionName;
	
	
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
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasConfiguration> dynamicConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas)");
		requestObject.save(dynamicAdminConsoleConfiguration, dynamicConfiguration);
		return "success";
	}
	
	public static class Request {
		@Expose private AuthenticationPanel authentication;
		@Expose private AuthorizationPanel authorization;
		@Expose private AuditPanel audit;
		
		
		
		/** TODO: Refactor for possible race condiation or deadlocks
		 * @throws Exception 
		 */
		public void save(DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration , DynamicObject<BindaasConfiguration> dynamicConfiguration ) throws Exception
		{
			BindaasAdminConsoleConfiguration adminConsoleConfiguration = dynamicAdminConsoleConfiguration.getObject();
			
			synchronized (adminConsoleConfiguration) {
				adminConsoleConfiguration.setAdminAccounts(this.authorization.adminAccounts);
				adminConsoleConfiguration.getAdminConfiguration().setAuthenticationMethod(this.authentication.admin.method);
				adminConsoleConfiguration.getAdminConfiguration().setLdapDNPattern(this.authentication.admin.ldapDNPattern);
				adminConsoleConfiguration.getAdminConfiguration().setLdapUrl(this.authentication.admin.ldapUrl);
				adminConsoleConfiguration.getAdminConfiguration().setOpenIdProviders(this.authentication.admin.openIdProviders);
				
				adminConsoleConfiguration.getUserConfiguration().setAuthenticationMethod(this.authentication.user.method);
				adminConsoleConfiguration.getUserConfiguration().setLdapDNPattern(this.authentication.user.ldapDNPattern);
				adminConsoleConfiguration.getUserConfiguration().setLdapUrl(this.authentication.user.ldapUrl);
				adminConsoleConfiguration.getUserConfiguration().setOpenIdProviders(this.authentication.user.openIdProviders);
				
				dynamicAdminConsoleConfiguration.saveObject();
				
			}
			
			BindaasConfiguration bindaasConfiguration = dynamicConfiguration.getObject();
			synchronized (bindaasConfiguration) {
				bindaasConfiguration.setEnableAudit(this.audit.enable);
				bindaasConfiguration.setEnableAuthentication(adminConsoleConfiguration.getUserConfiguration().getAuthenticationMethod() == AuthenticationMethod.none ? false : true);
				bindaasConfiguration.setEnableAuthorization(this.authorization.enableMethodAuthorization);
				dynamicConfiguration.saveObject();
			}
			
		}
	}
	
	public static class AuthenticationPanel {
		@Expose private UserPanel user;
		@Expose private AdminPanel admin;
		
		public static class UserPanel {
			@Expose private UserConfiguration.AuthenticationMethod method;
			@Expose private String ldapDNPattern;
			@Expose private String ldapUrl;
			@Expose private Map<String,Boolean> openIdProviders;
		}
		public static class AdminPanel {
			@Expose private AdminConfiguration.AuthenticationMethod method;
			@Expose private String ldapDNPattern;
			@Expose private String ldapUrl;
			@Expose private Map<String,Boolean> openIdProviders;
		}
	}
	public static class AuthorizationPanel {
		@Expose private Set<String> adminAccounts;
		@Expose private boolean enableMethodAuthorization;
	}
	public static class AuditPanel {
		@Expose private Boolean enable;
	}

}
