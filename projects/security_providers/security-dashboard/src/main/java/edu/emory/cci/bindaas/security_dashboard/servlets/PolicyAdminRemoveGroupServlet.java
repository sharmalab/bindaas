package edu.emory.cci.bindaas.security_dashboard.servlets;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.model.Group;
import edu.emory.cci.bindaas.security_dashboard.util.RakshakUtils;

/**
 *   /dashboard/security/policy-admin/remove-group
 * @author nadir
 *
 */
public class PolicyAdminRemoveGroupServlet extends RegistrableServlet{

	private static final long serialVersionUID = 1L;
	private String templateName;
	private Template template;
	private Log log = LogFactory.getLog(getClass());
	
	private IPolicyManager policyManager;
	private IAPIKeyManager apiKeyManager;
	
	public IAPIKeyManager getApiKeyManager() {
		return apiKeyManager;
	}

	public void setApiKeyManager(IAPIKeyManager apiKeyManager) {
		this.apiKeyManager = apiKeyManager;
	}

	

	public IPolicyManager getPolicyManager() {
		return policyManager;
	}

	public void setPolicyManager(IPolicyManager policyManager) {
		this.policyManager = policyManager;
	}

	
	public void init()
	{
		template = getVelocityEngineWrapper().getVelocityTemplateByName(templateName);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try{
		String content = IOUtils.toString(req.getInputStream());
		Payload payload = GSONUtil.getGSONInstance().fromJson(content, Payload.class);
		
		String resource = String.format("%s/%s/%s/%s", payload.project,
				payload.dataProvider, payload.type, payload.apiName);
		
		policyManager.removeAuthorizedMember(resource, payload.listOfGroups);
		
		}
		catch(Exception e)
		{
			log.error(e);
			throw new ServletException(e);
		}
		
		
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try {
		
		String project = req.getParameter("project");
		String dataProvider = req.getParameter("dataProvider");
		String type = req.getParameter("type");
		String apiName = req.getParameter("apiName");
		
		String resource = String.format("%s/%s/%s/%s", project , dataProvider , type , apiName);
		
		Set<String> alreadyAddedGroups = policyManager.getAuthorizedGroups(resource);
		Set<Group> remoteGroups = RakshakUtils.getAllGroups(getConfiguration() , apiKeyManager);
		Set<Group> finalGroups = new TreeSet<Group>();
		for(Group group : remoteGroups)
		{
			if(alreadyAddedGroups.contains(group.getName())){
				finalGroups.add(group);
			}
		}
		
		VelocityContext context = getVelocityEngineWrapper().createVelocityContext(req);
		context.put("groups" , finalGroups);
		context.put("project", project);
		context.put("dataProvider", dataProvider);
		context.put("type", type);
		context.put("apiName", apiName);
		template.merge(context, resp.getWriter());
		}
		catch(Exception e)
		{
			log.error(e);
			throw new ServletException(e);
		}
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	
	private static class Payload {
		@Expose private String project;
		@Expose private String dataProvider;
		@Expose private String type;
		@Expose private String apiName;
		@Expose private Set<String> listOfGroups;
		
	}	

}
