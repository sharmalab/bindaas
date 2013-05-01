package edu.emory.cci.bindaas.webconsole.admin;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;

public class EmailConfigurationPanelAction implements IAdminAction{

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
		DynamicProperties emailProperties = Activator.getService(DynamicProperties.class , "(name=mailService)");
		requestObject.saveToDynamicProperties(emailProperties);
		return "success";
	}
	
	
public static class Request {
		
		@Expose String smtpServer;
		@Expose Integer smtpPort;
		@Expose String username;
		
		public String getSmtpServer() {
			return smtpServer;
		}

		public void setSmtpServer(String smtpServer) {
			this.smtpServer = smtpServer;
		}

		public Integer getSmtpPort() {
			return smtpPort;
		}

		public void setSmtpPort(Integer smtpPort) {
			this.smtpPort = smtpPort;
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

		@Expose String password;
		
		public static Request fromDynamicProperties(DynamicProperties dynamicProperties)
		{
			Request request = new Request();
			request.smtpServer = (String) dynamicProperties.get("mail.smtp.host");
			request.smtpPort = dynamicProperties.get("mail.smtp.port") !=null ? Integer.parseInt(dynamicProperties.get("mail.smtp.port")) :  null;
			request.username = (String) dynamicProperties.get("username");
			request.password = (String) dynamicProperties.get("password");
			return request;
			
		}
		
		public void saveToDynamicProperties(DynamicProperties dynamicProperties)
		{
			dynamicProperties.put("mail.smtp.host", smtpServer);
			dynamicProperties.put("mail.smtp.port", smtpPort.toString());
			dynamicProperties.put("username", username);
			dynamicProperties.put("password", password);
		}
	}
}
