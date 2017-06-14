package edu.emory.cci.bindaas.webconsole.admin;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;

public class ServerAdministrationPanelAction implements IAdminAction {
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
		
		@Expose private String host;
		@Expose private Integer webconsolePort;
		@Expose private Integer middlewarePort;
		@Expose private Boolean enableProxy;
		@Expose private String webconsoleProxy;
		@Expose private String middlewareProxy;
		@Expose private String instanceName;
		
		public void save(DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration , DynamicObject<BindaasConfiguration> dynamicConfiguration ) throws Exception
		{
			BindaasAdminConsoleConfiguration adminConsoleConfiguration = dynamicAdminConsoleConfiguration.getObject();
			
			synchronized (adminConsoleConfiguration) {
				adminConsoleConfiguration.setEnableProxy(this.enableProxy);
				adminConsoleConfiguration.setHost(this.host);
				adminConsoleConfiguration.setPort(webconsolePort);
				adminConsoleConfiguration.setProxyUrl(webconsoleProxy);
				dynamicAdminConsoleConfiguration.saveObject();
				
			}
			
			BindaasConfiguration bindaasConfiguration = dynamicConfiguration.getObject();
			synchronized (bindaasConfiguration) {
				bindaasConfiguration.setProxyUrl(middlewareProxy);
				bindaasConfiguration.setPort(middlewarePort);
				bindaasConfiguration.setHost(host);
				bindaasConfiguration.setInstanceName(instanceName);
				dynamicConfiguration.saveObject();
			}
			
		}
	}

}
