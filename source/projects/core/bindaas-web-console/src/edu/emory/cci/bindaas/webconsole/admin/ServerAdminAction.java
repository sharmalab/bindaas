package edu.emory.cci.bindaas.webconsole.admin;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.api.BindaasConstants;
import edu.emory.cci.bindaas.core.util.DynamicProperties;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.admin.EmailAction.Request;

public class ServerAdminAction implements IAdminAction {
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
		DynamicProperties bindaasProperties = Activator.getService(DynamicProperties.class , "(name=bindaas)");
		requestObject.saveToDynamicProperties(bindaasProperties);
		return "success";
	}
	
	public static class Request {
		
		@Expose String webConsoleURL;
		@Expose String middlewareURL;
		@Expose String middlewareHost;
		public String getWebConsoleURL() {
			return webConsoleURL;
		}

		public void setWebConsoleURL(String webConsoleURL) {
			this.webConsoleURL = webConsoleURL;
		}

		public String getMiddlewareURL() {
			return middlewareURL;
		}

		public void setMiddlewareURL(String middlewareURL) {
			this.middlewareURL = middlewareURL;
		}

		public String getMiddlewareHost() {
			return middlewareHost;
		}

		public void setMiddlewareHost(String middlewareHost) {
			this.middlewareHost = middlewareHost;
		}

		public Integer getMiddlewarePort() {
			return middlewarePort;
		}

		public void setMiddlewarePort(Integer middlewarePort) {
			this.middlewarePort = middlewarePort;
		}

		@Expose Integer middlewarePort;
		
		public static Request fromDynamicProperties(DynamicProperties dynamicProperties)
		{
			Request request = new Request();
			request.webConsoleURL = (String) dynamicProperties.get(BindaasConstants.SERVICE_UI_URL);
			request.middlewareURL = (String) dynamicProperties.get(BindaasConstants.SERVICE_URL);
			request.middlewareHost = (String) dynamicProperties.get(BindaasConstants.HOST);
			request.middlewarePort = dynamicProperties.get(BindaasConstants.PORT)!=null ? Integer.parseInt(dynamicProperties.get(BindaasConstants.PORT)) : null;
			return request;
		}
		
		public void saveToDynamicProperties(DynamicProperties dynamicProperties)
		{
			dynamicProperties.put(BindaasConstants.SERVICE_UI_URL, webConsoleURL);
			dynamicProperties.put(BindaasConstants.SERVICE_URL, middlewareURL);
			dynamicProperties.put(BindaasConstants.HOST, middlewareHost);
			dynamicProperties.put(BindaasConstants.PORT, middlewarePort.toString());
		} 
		
	}

}
