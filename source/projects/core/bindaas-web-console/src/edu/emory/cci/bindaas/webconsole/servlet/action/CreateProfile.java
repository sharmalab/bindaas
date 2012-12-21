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
import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;


public class CreateProfile extends AbstractRequestHandler{

	private static String templateName = "createProfile.vt";
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
			generateCreateProfileView(request, response , pathParameters);
		}
		else if (request.getMethod().equalsIgnoreCase("post"))
		{
			doCreateProfile(request, response , pathParameters);
		}
		else
		{
			throw new Exception("Http Method [" + request.getMethod() + "] not allowed here");
		}
	}
	
	/**
	 *  /
	 * @param request
	 * @param response
	 * @param pathParameters 
	 */
	private void generateCreateProfileView(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
	{
		VelocityContext context = new VelocityContext(pathParameters);
		IProviderRegistry providerRegistry = Activator.getProviderRegistry();
		if(providerRegistry!=null)
		{
			Collection<IProvider> listOfProviders = providerRegistry.findProviders();
			context.put("providers" , listOfProviders);
			context.put("esc", Activator.getEscapeTool());
			try {
				template.merge(context, response.getWriter());
			} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
			}
		}
		else
		{
			log.error("IProviderRegistry not available");
			ErrorView.handleError(response, new Exception() );
		}
		
		
	}
	
	private void doCreateProfile(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
	{
		String workspace = pathParameters.get("workspace");
		String profileName = request.getParameter("profileName");
		String jsonRequest = request.getParameter("jsonRequest");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		IManagementTasks managementTask = Activator.getManagementTasksBean();
		try {
			Profile profile = managementTask.createProfile(profileName, workspace, jsonObject, createdBy);
			response.setContentType(StandardMimeType.JSON.toString());
			response.getWriter().append(profile.toString());
			response.getWriter().flush();
		} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
		}
		
	}

}
