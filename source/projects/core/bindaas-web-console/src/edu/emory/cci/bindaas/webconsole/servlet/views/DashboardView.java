package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;

public class DashboardView extends AbstractRequestHandler {

	private static String templateName = "dashboard.vt";
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
		if(managementTasks!=null)
		{
			Collection<Workspace> workspaces = managementTasks.listWorkspaces();
			VelocityContext context = new VelocityContext();
			context.put("workspaces", workspaces);
			template.merge(context, response.getWriter());
		}
		else
		{
			log.error("IManagementTasks service not available");
			throw new Exception("Service not available");
		}
		
	}

}
