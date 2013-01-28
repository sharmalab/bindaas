package edu.emory.cci.bindaas.webconsole.admin;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.admin.CheckLDAPConnectionAction.Request;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;

public class UserManagementNotificationPanelAction  implements IAdminAction{
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
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		requestObject.save(dynamicAdminConsoleConfiguration);
		return "success";
		
	}
	
	public static class Request {
		
		
		@Expose private Boolean enableNotification;
		@Expose private List<String> notificationRecepients;
		
		public void save(DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration) throws Exception
		{
			BindaasAdminConsoleConfiguration adminConsoleConfiguration = dynamicAdminConsoleConfiguration.getObject();
			synchronized (adminConsoleConfiguration) {
				adminConsoleConfiguration.getUserAccountManagement().setEnableUserSignupNotification(enableNotification);
				adminConsoleConfiguration.getUserAccountManagement().setNotificationRecepients(notificationRecepients);
				dynamicAdminConsoleConfiguration.saveObject();
			}
		}
	}

}
