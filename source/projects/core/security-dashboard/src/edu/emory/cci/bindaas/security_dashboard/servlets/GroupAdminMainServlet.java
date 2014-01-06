package edu.emory.cci.bindaas.security_dashboard.servlets;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.model.Group;
import edu.emory.cci.bindaas.security_dashboard.util.RakshakUtils;

public class GroupAdminMainServlet extends RegistrableServlet{

	private static final long serialVersionUID = 1L;
	private String templateName;
	private Template template;
	private IManagementTasks managementTask;
	private Log log = LogFactory.getLog(getClass());
	private IPolicyManager policyManager;
	

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

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}



	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
	
		try {
			  SecurityDashboardConfiguration config = getConfiguration();
			  Set<Group> groups = RakshakUtils.getAllGroups(config);
			  VelocityContext context = getVelocityEngineWrapper().createVelocityContext(req);
			  
			  context.put("groups", groups);
			  template.merge(context, resp.getWriter());
			  
		} catch (Exception e) {
			log.error(e);
			throw new ServletException(e);
		}
		
	}
	
	
	
	public IManagementTasks getManagementTask() {
		return managementTask;
	}

	public void setManagementTask(IManagementTasks managementTask) {
		this.managementTask = managementTask;
	}
	
	

}
