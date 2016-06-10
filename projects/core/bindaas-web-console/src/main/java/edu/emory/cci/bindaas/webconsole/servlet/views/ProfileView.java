package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.security.Principal;
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
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.version_manager.api.IVersionManager;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;

public class ProfileView extends AbstractRequestHandler {

	private static String templateName = "profile.vt";
	private  Template template;
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	private VelocityEngineWrapper velocityEngineWrapper;
	private IManagementTasks managementTasks;
	private IVersionManager versionManager;
	private IProviderRegistry providerRegistry;
	
	public IProviderRegistry getProviderRegistry() {
		return providerRegistry;
	}

	public void setProviderRegistry(IProviderRegistry providerRegistry) {
		this.providerRegistry = providerRegistry;
	}

	
	public IManagementTasks getManagementTasks() {
		return managementTasks;
	}

	public void setManagementTasks(IManagementTasks managementTasks) {
		this.managementTasks = managementTasks;
	}

	public IVersionManager getVersionManager() {
		return versionManager;
	}

	public void setVersionManager(IVersionManager versionManager) {
		this.versionManager = versionManager;
	}


	
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
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {
		
		
		if(request.getMethod().equalsIgnoreCase("get"))
		{
			generateView(request, response, pathParameters);
		}
		else if (request.getMethod().equalsIgnoreCase("post"))
		{
			updateProfile(request, response, pathParameters);
		}
		else if (request.getMethod().equalsIgnoreCase("delete"))
		{
			deleteProfile(request, response, pathParameters);
		}
		else
		{
			throw new Exception("Http Method [" + request.getMethod() + "] not allowed here");
		}
		
	}
	
	
	public void generateView(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters) throws Exception
	{
		
		if(managementTasks!=null)
		{
			String workspace = pathParameters.get("workspace");
			String profileName = pathParameters.get("profile");
			
			Profile profile = managementTasks.getProfile(workspace, profileName); 
			VelocityContext context = new VelocityContext(pathParameters);
			context.put("esc", velocityEngineWrapper.getEscapeTool());
			context.put("profile", profile);
			context.put("bindaasUser" , BindaasUser.class.cast(request.getSession().getAttribute("loggedInUser")).getName());
			/**
			 * Add version information
			 */
			String versionHeader = String.format("System built <strong>%s</strong>  Build date <strong>%s<strong>", versionManager.getSystemBuild() ,versionManager.getSystemBuildDate());;
			context.put("versionHeader", versionHeader);
			
			
			if(providerRegistry!=null)
			{
				IProvider provider = providerRegistry.lookupProvider(profile.getProviderId(), profile.getProviderVersion());
				context.put("provider" , provider);
			}
			else
			{
				log.error("IProviderRegistry not available");
				ErrorView.handleError(response, new Exception() );
			}
	
			template.merge(context, response.getWriter());
		}
		else
		{
			log.error("IManagementTasks service not available");
			throw new Exception("Service not available");
		}
	}
	
	public void updateProfile(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
	{
		String workspace = pathParameters.get("workspace");
		String profileName = pathParameters.get("profile");
		String description = request.getParameter("description");
		String createdBy = ((Principal)request.getSession().getAttribute("loggedInUser")).getName();
		String jsonRequest = request.getParameter("jsonRequest");
		JsonObject jsonObject = GSONUtil.getJsonParser().parse(jsonRequest).getAsJsonObject();
		
		
		try {
			if(managementTasks!=null){ 
				Profile profile = managementTasks.updateProfile(profileName, workspace, jsonObject, createdBy , description);
				response.setContentType(StandardMimeType.JSON.toString());
				response.getWriter().append(profile.toString());
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
	
public void deleteProfile(HttpServletRequest request,
		HttpServletResponse response, Map<String, String> pathParameters) 
{
	String workspace = pathParameters.get("workspace");
	String profileName = pathParameters.get("profile");
	
	
	try {
		if(managementTasks!=null){
			managementTasks.deleteProfile(workspace,profileName);
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
