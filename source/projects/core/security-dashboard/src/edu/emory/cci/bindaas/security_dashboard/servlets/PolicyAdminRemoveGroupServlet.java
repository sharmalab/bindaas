package edu.emory.cci.bindaas.security_dashboard.servlets;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.model.Group;

public class PolicyAdminRemoveGroupServlet extends RegistrableServlet{

	private static final long serialVersionUID = 1L;
	private String templateName;
	private Template template;
	
	
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

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String project = req.getParameter("project");
		String dataProvider = req.getParameter("dataProvider");
		String type = req.getParameter("type");
		String apiName = req.getParameter("apiName");
		
		String resource = String.format("%s/%s/%s/%s", project , dataProvider , type , apiName);
		
		Set<Group> alreadyAddedGroups = policyManager.getAuthorizedGroups(resource);

		VelocityContext context = getVelocityEngineWrapper().createVelocityContext();
		context.put("groups" , alreadyAddedGroups);
		
		template.merge(context, resp.getWriter());
	}
	
	
	
	
	
	

}
