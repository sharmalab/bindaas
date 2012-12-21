package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.api.BindaasConstants;
import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.rest.service.api.IBindaasAdminService;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;

public class QueryBrowserView extends AbstractRequestHandler {

	private static String templateName = "queryBrowser.vt";
	private static Template template;
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	
	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	static {
		template = Activator.getVelocityTemplateByName(templateName);
	}


	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {
		IManagementTasks managementTasks = Activator.getManagementTasksBean();
		IBindaasAdminService adminService = Activator.getBindaasAdminService();
		if(managementTasks!=null && adminService!=null)
		{
			Collection<Workspace> workspaces = managementTasks.listWorkspaces();
			VelocityContext context = new VelocityContext();
			context.put("workspaces", workspaces);
			
			String serviceUrl = adminService.getProperty(BindaasConstants
					.SERVICE_URL);
			context.put("serviceUrl", serviceUrl);
			template.merge(context, response.getWriter());
		}
		else
		{
			log.error("IManagementTasks and/or IBindaasAdminService service not available");
			throw new Exception("Service not available");
		}
		
	}

}
