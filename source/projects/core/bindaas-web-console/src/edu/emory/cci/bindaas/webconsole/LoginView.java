package edu.emory.cci.bindaas.webconsole;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.installer.command.VersionCommand;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;

public class LoginView {
	
	private  String templateName = "login.vt";
	private  Template template;
	private  Log log = LogFactory.getLog(LoginView.class);
	private VelocityEngineWrapper velocityEngineWrapper;
	private VersionCommand versionCommand;
	
	
	public VersionCommand getVersionCommand() {
		return versionCommand;
	}



	public void setVersionCommand(VersionCommand versionCommand) {
		this.versionCommand = versionCommand;
	}



	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}



	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	
	public void generateLoginView(HttpServletRequest request , HttpServletResponse response , String loginTarget , String errorMessage) throws Exception
	{
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		VelocityContext context = new VelocityContext();
		/**
		 * Add version information
		 */
		String versionHeader = "";
		//VersionCommand versionCommand = Activator.getService(VersionCommand.class);
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
		context.put("loginTarget", loginTarget);
		context.put("errorMessage", errorMessage);
		context.put("adminconsoleConfiguration", dynamicAdminConsoleConfiguration.getObject().clone());
		
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
