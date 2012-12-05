package edu.emory.cci.bindaas.webconsole.servlet.action;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;

public class CreateSubmitEndpoint extends AbstractRequestHandler{
	private static String templateName = "createSubmitEndpoint.vt";
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
			HttpServletResponse response, Map<String,String> pathParameters) throws Exception {

		if(request.getMethod().equalsIgnoreCase("get"))
		{
			generateView(request, response , pathParameters );
		}
		else if (request.getMethod().equalsIgnoreCase("post"))
		{
			doAction(request, response , pathParameters);
		}
		else
		{
			throw new Exception("Http Method [" + request.getMethod() + "] not allowed here");
		}
	}
	
	
	private void generateView(HttpServletRequest request,
			HttpServletResponse response , Map<String,String> pathParameters)
	{
		VelocityContext context = new VelocityContext(pathParameters);
		IModifierRegistry modifierRegistry = Activator.getModifierRegistry();
		Collection<ISubmitPayloadModifier> submitPayloadModifier = modifierRegistry.findAllSubmitPayloadModifiers();
		context.put("submitPayloadModifiers" , submitPayloadModifier);
		
		try {
			template.merge(context, response.getWriter());
		} catch (Exception e) {
			log.error(e);
			ErrorView.handleError(response, e);
		}
		
	}
	
	private void doAction(HttpServletRequest request,
			HttpServletResponse response , Map<String,String> pathParameters)
	{
		String workspace = pathParameters.get("workspace");
		String profile = pathParameters.get("profile");
		String submitEndpointName = request.getParameter("submitEndpointName");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		String jsonRequest = request.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		IManagementTasks managementTask = Activator.getManagementTasksBean();
		try {
			SubmitEndpoint queryEndpoint = managementTask.createSubmitEndpoint(submitEndpointName, workspace, profile, jsonObject, createdBy);
			response.setContentType(StandardMimeType.JSON.toString());
			response.getWriter().append(queryEndpoint.toString());
			response.getWriter().flush();
		} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
		}
	}

	
}
