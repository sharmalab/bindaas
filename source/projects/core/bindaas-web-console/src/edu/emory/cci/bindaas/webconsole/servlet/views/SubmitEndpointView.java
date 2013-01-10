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
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;

public class SubmitEndpointView extends AbstractRequestHandler {

	private static String templateName = "submitEndpoint.vt";
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
			updateSubmitEndpoint(request, response, pathParameters);
		}
		else if (request.getMethod().equalsIgnoreCase("delete"))
		{
			deleteSubmitEndpoint(request, response, pathParameters);
		}
		else
		{
			throw new Exception("Http Method [" + request.getMethod() + "] not allowed here");
		}
		
	}
	
	
	public void generateView(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters) throws Exception
	{
		IManagementTasks managementTasks = Activator.getService(IManagementTasks.class);
		if(managementTasks!=null)
		{
			String workspace = pathParameters.get("workspace");
			String profile = pathParameters.get("profile");
			String submitEndpointName = pathParameters.get("submitEndpoint");
			
			SubmitEndpoint submitEndpoint = managementTasks.getSubmitEndpoint(workspace, profile, submitEndpointName); 
			VelocityContext context = new VelocityContext(pathParameters);
			context.put("esc", Activator.getEscapeTool());
			context.put("submitEndpoint", submitEndpoint);
			context.put("bindaasUser" , BindaasUser.class.cast(request.getSession().getAttribute("loggedInUser")).getName());
			
			IModifierRegistry modifierRegistry = Activator.getService(IModifierRegistry.class);
			Collection<ISubmitPayloadModifier> submitPayloadModifier = modifierRegistry.findAllSubmitPayloadModifiers();
			context.put("submitPayloadModifiers" , submitPayloadModifier);
			
			Profile prof = managementTasks.getProfile(pathParameters.get("workspace"), pathParameters.get("profile"));
			JsonObject documentation = Activator.getService(IProviderRegistry.class).lookupProvider(prof.getProviderId(), prof.getProviderVersion()).getDocumentation(); // TODO : NullPointer Traps here . 
			context.put("documentation" , documentation);
			template.merge(context, response.getWriter());
		}
		else
		{
			log.error("IManagementTasks service not available");
			throw new Exception("Service not available");
		}
	}
	
	public void updateSubmitEndpoint(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
	{
		String workspace = pathParameters.get("workspace");
		String profile = pathParameters.get("profile");
		String submitEndpointName = request.getParameter("submitEndpointName");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		String jsonRequest = request.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		IManagementTasks managementTask = Activator.getService(IManagementTasks.class);
		try {
			if(managementTask!=null){
				SubmitEndpoint submitEndpoint = managementTask.updateSubmitEndpoint(submitEndpointName, workspace, profile, jsonObject, createdBy);
				response.setContentType(StandardMimeType.JSON.toString());
				response.getWriter().append(submitEndpoint.toString());
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
	
	
	public void deleteSubmitEndpoint(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters) 
	{
		String workspace = pathParameters.get("workspace");
		String profile = pathParameters.get("profile");
		String submitEndpointName = pathParameters.get("submitEndpoint");
		
		IManagementTasks managementTask = Activator.getService(IManagementTasks.class);
		try {
			if(managementTask!=null){
				SubmitEndpoint submitEndpoint = managementTask.deleteSubmitEndpoint(workspace, profile, submitEndpointName);
				response.setContentType(StandardMimeType.JSON.toString());
				response.getWriter().append(submitEndpoint.toString());
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
