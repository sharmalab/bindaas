package edu.emory.cci.bindaas.webconsole.admin;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider;


public class CheckLDAPConnectionAction implements IAdminAction {
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
		if(requestObject.testLDAPConnection())
		{
			return "<label>LDAP settings verified !</label>";
		}
		else
		{
			return "<label class='error'>Could not authenticate the user.Please check the connection properties and ensure correct username/password is entered <label>";
		}
		
	}
	
	public static class Request {
		
		
		@Expose String webConsoleLDAPServer;
		@Expose String webConsoleDNPattern;
		@Expose String username;
		@Expose String password;
		
		public boolean testLDAPConnection()
		{
			Properties props = new Properties();
			props.put("ldap.url", webConsoleLDAPServer);
			props.put("ldap.dn.pattern", webConsoleDNPattern);
			return LDAPAuthenticationProvider.testConnection(props, username, password);
		}
	}

}
