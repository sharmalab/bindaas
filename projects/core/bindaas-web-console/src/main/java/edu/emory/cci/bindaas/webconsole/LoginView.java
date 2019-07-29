package edu.emory.cci.bindaas.webconsole;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.jwt.IJWTManager;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.version_manager.api.IVersionManager;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;

public class LoginView {
	
	private  String templateName = "login.vt";
	private  Template template;
	private VelocityEngineWrapper velocityEngineWrapper;
	private IVersionManager versionManager;
	private IJWTManager JWTManager;


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



	public IJWTManager getJWTManager() {
		return JWTManager;
	}

	public void setJWTManager(IJWTManager JWTManager) {
		this.JWTManager = JWTManager;
	}


	public void generateLoginView(HttpServletRequest request , HttpServletResponse response , String loginTarget , String errorMessage) throws Exception
	{
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasConfiguration> bindaasConfiguration = Activator.getService(DynamicObject.class , "(name=bindaas)");
		VelocityContext context = new VelocityContext();
		/**
		 * Add version information
		 */
		String versionHeader = String.format("System built <strong>%s</strong>  Build date <strong>%s<strong>", versionManager.getSystemBuild() ,versionManager.getSystemBuildDate());
		
		context.put("versionHeader", versionHeader);
		context.put("loginTarget", loginTarget);
		context.put("errorMessage", errorMessage);
		context.put("adminconsoleConfiguration", dynamicAdminConsoleConfiguration.getObject().clone());
		context.put("bindaasConfiguration", bindaasConfiguration.getObject().clone());
		context.put("auth0Domain", this.JWTManager.getAuth0Domain());
		context.put("auth0ClientId", this.JWTManager.getAuth0ClientId());
		context.put("auth0Audience", this.JWTManager.getAuth0Audience());

		// lazy init
		if(template == null)
		{
			if(velocityEngineWrapper!=null)
			{
				template = velocityEngineWrapper.getVelocityTemplateByName(templateName);
			}
			else
			{
				throw new Exception("VelocityEngineWrapper service not available");
			}
		}
		
		template.merge(context, response.getWriter());
		
	}
}
