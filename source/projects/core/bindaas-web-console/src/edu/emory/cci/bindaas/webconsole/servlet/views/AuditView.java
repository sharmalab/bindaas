package edu.emory.cci.bindaas.webconsole.servlet.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuditProvider;
import edu.emory.cci.bindaas.security.model.hibernate.AuditMessage;
import edu.emory.cci.bindaas.version_manager.api.IVersionManager;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;

public class AuditView extends AbstractRequestHandler {

	private static String templateName = "audit.vt";
	private Template template;
	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	private VelocityEngineWrapper velocityEngineWrapper;
	private IVersionManager versionManager;
	private IAuditProvider auditProvider;
	
	
	public IVersionManager getVersionManager() {
		return versionManager;
	}

	public void setVersionManager(IVersionManager versionManager) {
		this.versionManager = versionManager;
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
		String versionHeader = String.format("System built <strong>%s</strong>  Build date <strong>%s<strong>", versionManager.getSystemBuild() ,versionManager.getSystemBuildDate());;
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
		context.put("esc", velocityEngineWrapper.getEscapeTool());
		template.merge(context, response.getWriter());
	}

}
