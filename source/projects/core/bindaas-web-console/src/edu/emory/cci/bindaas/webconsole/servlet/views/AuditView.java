package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.installer.command.VersionCommand;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;

public class AuditView extends AbstractRequestHandler {

	private static String templateName = "audit.vt";
	private Template template;
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	private VelocityEngineWrapper velocityEngineWrapper;
	private VersionCommand versionCommand;
	private IAuditProvider auditProvider;
	
	
	public VersionCommand getVersionCommand() {
		return versionCommand;
	}

	public void setVersionCommand(VersionCommand versionCommand) {
		this.versionCommand = versionCommand;
	}

	public IAuditProvider getAuditProvider() {
		return auditProvider;
	}

	public void setAuditProvider(IAuditProvider auditProvider) {
		this.auditProvider = auditProvider;
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
		log.debug( getClass().getName() + " Initialized");
	}

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {

		if (request.getMethod().equalsIgnoreCase("get")) {
			generateView(request, response, pathParameters);
		}
		else {
			throw new Exception("Http Method [" + request.getMethod()
					+ "] not allowed here");
		}

	}

	public void generateView(HttpServletRequest request,
			HttpServletResponse response, Map<String, String> pathParameters)
			throws Exception {
		
		List<AuditMessage> messages = null;
		VelocityContext context = new VelocityContext();
		/**
		 * Add version information
		 */
		String versionHeader = "";
		
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
		context.put("versionHeader", versionHeader);		context.put(
				"bindaasUser",
				BindaasUser.class.cast(
						request.getSession().getAttribute("loggedInUser"))
						.getName());
		
		if (auditProvider != null) {
			messages = auditProvider.getAuditLogs();
			context.put("auditMessages", messages );
			
		} else {
			
			log.warn("No audit logs found");
			messages = new ArrayList<AuditMessage>();
			
		}
		template.merge(context, response.getWriter());
	}

}
