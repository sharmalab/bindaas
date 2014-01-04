package edu.emory.cci.bindaas.security_dashboard.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.model.Group;
import edu.emory.cci.bindaas.security_dashboard.util.RakshakUtils;

/**
 * getUserOrGroup(project,data-provider,type,apiname,requestType = add|remove)
 * @author nadir
 *
 */
public class GetGroups extends RegistrableServlet{
	private static final long serialVersionUID = 1L;
	
	private IPolicyManager policyManager;
	private String templateName;
	private Template template;
	
	
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


	public IPolicyManager getPolicyManager() {
		return policyManager;
	}


	public void setPolicyManager(IPolicyManager policyManager) {
		this.policyManager = policyManager;
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		String project = req.getParameter("project");
		String dataProvider = req.getParameter("dataProvider");
		String type = req.getParameter("type");
		String apiName = req.getParameter("apiName");
		String requestType = req.getParameter("requestType");
		
		
		SecurityDashboardConfiguration config = getConfiguration();
		
		if(requestType.equalsIgnoreCase("add"))
		{
			String html = processAddRequestType(project, dataProvider, apiName, type, config);
			resp.getWriter().write(html);
		}
		else if(requestType.equalsIgnoreCase("remove"))
		{
			String html = processRemoveRequestType(project, dataProvider, apiName, type, config);
			resp.getWriter().write(html);
		}
		else throw new ServletException("Unknow requestType specified [" + requestType + "]");
		
	}
	
	
	public String processAddRequestType(String project, String dataProvider , String apiName , String type , SecurityDashboardConfiguration config ) throws IOException
	{
		String resource = String.format("%s/%s/%s/%s", project , dataProvider , type , apiName);
		Set<Group> remoteGroups = getListOfRemoteGroups(config);
		Set<Group> alreadyAddedGroups = policyManager.getAuthorizedGroups(resource);
			
		remoteGroups.removeAll(alreadyAddedGroups); // remoteGroup has list of groups to be displayed
		VelocityContext context = getVelocityEngineWrapper().createVelocityContext();
		context.put("groups" , remoteGroups);
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		sw.close();
		return sw.toString();
	}
	
	private Set<Group> getListOfRemoteGroups(SecurityDashboardConfiguration config ) {

		return RakshakUtils.getAllGroups(config);
	}


	public String processRemoveRequestType(String project, String dataProvider , String apiName , String type , SecurityDashboardConfiguration config) throws IOException
	{
		String resource = String.format("%s/%s/%s/%s", project , dataProvider , type , apiName);
		
		Set<Group> alreadyAddedGroups = policyManager.getAuthorizedGroups(resource);

		VelocityContext context = getVelocityEngineWrapper().createVelocityContext();
		context.put("groups" , alreadyAddedGroups);
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		sw.close();
		return sw.toString();
	}
	
	
	

	
	
}
