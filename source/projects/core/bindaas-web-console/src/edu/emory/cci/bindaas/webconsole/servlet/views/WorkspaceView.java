package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;

public class WorkspaceView extends AbstractRequestHandler {

	private static String templateName = "workspace.vt";
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

		if (request.getMethod().equalsIgnoreCase("get")) {
			generateView(request, response, pathParameters);
		}

		else if (request.getMethod().equalsIgnoreCase("delete")) {
			deleteWorkspace(request, response, pathParameters);
		} else {
			throw new Exception("Http Method [" + request.getMethod()
					+ "] not allowed here");
		}

	}

	public void generateView(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {
		IManagementTasks managementTasks = Activator
				.getService(IManagementTasks.class);
		if (managementTasks != null) {
			Workspace workspace = managementTasks.getWorkspace(pathParameters
					.get("workspace"));
			VelocityContext context = new VelocityContext();
			context.put("workspace", workspace);
			context.put(
					"bindaasUser",
					BindaasUser.class.cast(
							request.getSession().getAttribute("loggedInUser"))
							.getName());
			template.merge(context, response.getWriter());
		} else {
			log.error("IManagementTasks service not available");
			throw new Exception("Service not available");
		}
	}

	public void deleteWorkspace(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters) {
		String workspace = pathParameters.get("workspace");

		IManagementTasks managementTask = Activator
				.getService(IManagementTasks.class);
		try {
			if (managementTask != null) {
				managementTask.deleteWorkspace(workspace);
				response.getWriter().append("success");
				response.getWriter().flush();
			} else {
				log.error("IManagementTasks service not available");
				throw new Exception("Service not available");
			}

		} catch (Exception e) {
			log.error(e);
			ErrorView.handleError(response, e);
		}

	}

}
