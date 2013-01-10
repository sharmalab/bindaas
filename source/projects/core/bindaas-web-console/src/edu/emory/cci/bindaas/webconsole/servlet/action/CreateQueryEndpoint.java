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
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.IRequestHandler;

public class CreateQueryEndpoint extends AbstractRequestHandler{
	private static String templateName = "createQueryEndpoint.vt";
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
		try {
		VelocityContext context = new VelocityContext(pathParameters);
		IModifierRegistry modifierRegistry = Activator.getService(IModifierRegistry.class);
		Collection<IQueryModifier> queryModifiers = modifierRegistry.findAllQueryModifier();
		Collection<IQueryResultModifier> queryResultModifiers = modifierRegistry.findAllQueryResultModifiers();
		context.put("queryModifiers" , queryModifiers);
		context.put("queryResultModifiers" , queryResultModifiers); 
		context.put("bindaasUser" , BindaasUser.class.cast(request.getSession().getAttribute("loggedInUser")).getName());
		
		IManagementTasks managementTask = Activator.getService(IManagementTasks.class);
		Profile profile = managementTask.getProfile(pathParameters.get("workspace"), pathParameters.get("profile"));
		JsonObject documentation = Activator.getService(IProviderRegistry.class).lookupProvider(profile.getProviderId(), profile.getProviderVersion()).getDocumentation(); // TODO : NullPointer Traps here . 
		context.put("documentation" , documentation); 
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
		String queryEndpointName = request.getParameter("queryEndpointName");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		String jsonRequest = request.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		IManagementTasks managementTask = Activator.getService(IManagementTasks.class);
		try {
			QueryEndpoint queryEndpoint = managementTask.createQueryEndpoint(queryEndpointName, workspace, profile, jsonObject, createdBy);
			response.setContentType(StandardMimeType.JSON.toString());
			response.getWriter().append(queryEndpoint.toString());
			response.getWriter().flush();
		} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
		}
	}

	
}
