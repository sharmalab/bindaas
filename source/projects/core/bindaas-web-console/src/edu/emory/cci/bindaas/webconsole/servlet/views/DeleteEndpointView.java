package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.EscapeTool;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;

public class DeleteEndpointView extends AbstractRequestHandler {

	private static String templateName = "deleteEndpoint.vt";
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
		
		
		if(request.getMethod().equalsIgnoreCase("get"))
		{
			generateView(request, response, pathParameters);
		}
		else if (request.getMethod().equalsIgnoreCase("post"))
		{
			updateDeleteEndpoint(request, response, pathParameters);
		}
		else if (request.getMethod().equalsIgnoreCase("delete"))
		{
			deleteDeleteEndpoint(request, response, pathParameters);
		}
		else
		{
			throw new Exception("Http Method [" + request.getMethod() + "] not allowed here");
		}
		
	}
	
	
	public void generateView(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters) throws Exception
	{
		IManagementTasks managementTasks = Activator.getManagementTasksBean();
		if(managementTasks!=null)
		{
			String workspace = pathParameters.get("workspace");
			String profile = pathParameters.get("profile");
			String deleteEndpointName = pathParameters.get("deleteEndpoint");
			
			DeleteEndpoint deleteEndpoint = managementTasks.getDeleteEndpoint(workspace, profile, deleteEndpointName); 
			VelocityContext context = new VelocityContext(pathParameters);
			context.put("esc", new EscapeTool());
			context.put("deleteEndpoint", deleteEndpoint);
			template.merge(context, response.getWriter());
		}
		else
		{
			log.error("IManagementTasks service not available");
			throw new Exception("Service not available");
		}
	}
	
	public void updateDeleteEndpoint(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
	{
		String workspace = pathParameters.get("workspace");
		String profile = pathParameters.get("profile");
		String deleteEndpointName = request.getParameter("deleteEndpointName");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		String jsonRequest = request.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		IManagementTasks managementTask = Activator.getManagementTasksBean();
		try {
			if(managementTask!=null){
				DeleteEndpoint deleteEndpoint = managementTask.updateDeleteEndpoint(deleteEndpointName, workspace, profile, jsonObject, createdBy);
				response.setContentType(StandardMimeType.JSON.toString());
				response.getWriter().append(deleteEndpoint.toString());
				response.getWriter().flush();
			}
			else
			{
				log.error("IManagementTasks service not available");
				throw new Exception("Service not available");
			}
			
		} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
		}

	}
	
	public void deleteDeleteEndpoint(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters) 
	{
		String workspace = pathParameters.get("workspace");
		String profile = pathParameters.get("profile");
		String deleteEndpointName = pathParameters.get("deleteEndpoint");
		
		IManagementTasks managementTask = Activator.getManagementTasksBean();
		try {
			if(managementTask!=null){
				DeleteEndpoint deleteEndpoint = managementTask.deleteDeleteEndpoint(workspace, profile, deleteEndpointName);
				response.setContentType(StandardMimeType.JSON.toString());
				response.getWriter().append(deleteEndpoint.toString());
				response.getWriter().flush();
			}
			else
			{
				log.error("IManagementTasks service not available");
				throw new Exception("Service not available");
			}
			
		} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
		}

	}

	
	

}
