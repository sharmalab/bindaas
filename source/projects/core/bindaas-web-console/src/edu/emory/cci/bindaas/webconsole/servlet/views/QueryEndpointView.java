package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.security.Principal;
import java.util.Collection;
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
import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;

public class QueryEndpointView extends AbstractRequestHandler {

	private static String templateName = "queryEndpoint.vt";
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
			updateQueryEndpoint(request, response, pathParameters);
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
			String queryEndpointName = pathParameters.get("queryEndpoint");
			
			QueryEndpoint queryEndpoint = managementTasks.getQueryEndpoint(workspace, profile, queryEndpointName); 
			VelocityContext context = new VelocityContext(pathParameters);
			context.put("esc", new EscapeTool());
			context.put("queryEndpoint", queryEndpoint);
			
			if(queryEndpoint.getQueryModifiers()!=null)
				context.put("hasQueryModifier", new Boolean(true));
			else
				context.put("hasQueryModifier", new Boolean(false));
			
			if(queryEndpoint.getQueryResultModifiers()!=null)
				context.put("hasQueryResultModifier", new Boolean(true));
			else
				context.put("hasQueryResultModifier", new Boolean(false));
			
			IModifierRegistry modifierRegistry = Activator.getModifierRegistry();
			Collection<IQueryModifier> queryModifiers = modifierRegistry.findAllQueryModifier();
			Collection<IQueryResultModifier> queryResultModifiers = modifierRegistry.findAllQueryResultModifiers();
			context.put("queryModifiers" , queryModifiers);
			context.put("queryResultModifiers" , queryResultModifiers);
			
			template.merge(context, response.getWriter());
		}
		else
		{
			log.error("IManagementTasks service not available");
			throw new Exception("Service not available");
		}
	}
	
	public void updateQueryEndpoint(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
	{
		String workspace = pathParameters.get("workspace");
		String profile = pathParameters.get("profile");
		String queryEndpointName = request.getParameter("queryEndpointName");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		String jsonRequest = request.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		IManagementTasks managementTask = Activator.getManagementTasksBean();
		try {
			if(managementTask!=null){
				QueryEndpoint queryEndpoint = managementTask.updateQueryEndpoint(queryEndpointName, workspace, profile, jsonObject, createdBy);
				response.setContentType(StandardMimeType.JSON.toString());
				response.getWriter().append(queryEndpoint.toString());
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
