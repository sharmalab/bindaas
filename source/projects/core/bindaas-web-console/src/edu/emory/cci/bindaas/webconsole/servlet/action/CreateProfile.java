package edu.emory.cci.bindaas.webconsole.servlet.action;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

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
import edu.emory.cci.bindaas.installer.command.VersionCommand;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;


public class CreateProfile extends AbstractRequestHandler{

	private static String templateName = "createProfile.vt";
	private  Template template;
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	private VelocityEngineWrapper velocityEngineWrapper;
	
	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	public void init() throws Exception
	{
		template = velocityEngineWrapper.getVelocityTemplateByName(templateName);
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
		IProviderRegistry providerRegistry = Activator.getService(IProviderRegistry.class);
		if(providerRegistry!=null)
		{
			Collection<IProvider> listOfProviders = providerRegistry.findProviders();
			context.put("providers" , listOfProviders);
			context.put("esc", velocityEngineWrapper.getEscapeTool());
			context.put("bindaasUser" , BindaasUser.class.cast(request.getSession().getAttribute("loggedInUser")).getName());
			/**
			 * Add version information
			 */
			String versionHeader = "";
			VersionCommand versionCommand = Activator.getService(VersionCommand.class);
			if(versionCommand!=null)
			{
				String frameworkBuilt = "";
			
				String buildDate = "";
				try{
					Properties versionProperties = versionCommand.getProperties();
					frameworkBuilt = String.format("%s.%s.%s", versionProperties.get("bindaas.framework.version.major") , versionProperties.get("bindaas.framework.version.minor") , versionProperties.get("bindaas.framework.version.revision") );
			
					buildDate = versionProperties.getProperty("bindaas.build.date");
				}catch(NullPointerException e)
				{
					log.warn("Version Header not set");
				}
				versionHeader = String.format("System built <strong>%s</strong>  Build date <strong>%s<strong>", frameworkBuilt,buildDate);
			}
			else
			{
				log.warn("Version Header not set");
			}
			context.put("versionHeader", versionHeader);
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
		String description = request.getParameter("description");
		String jsonRequest = request.getParameter("jsonRequest");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		IManagementTasks managementTask = Activator.getService(IManagementTasks.class);
		try {
			Profile profile = managementTask.createProfile(profileName, workspace, jsonObject, createdBy , description);
			response.setContentType(StandardMimeType.JSON.toString());
			response.getWriter().append(profile.toString());
			response.getWriter().flush();
		} catch (Exception e) {
				log.error(e);
				ErrorView.handleError(response, e);
		}
		
	}

}
