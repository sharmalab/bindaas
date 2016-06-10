package edu.emory.cci.bindaas.security_dashboard.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.google.common.base.Joiner;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.framework.model.DeleteEndpoint;
import edu.emory.cci.bindaas.framework.model.Profile;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;

/**
 *   /dashboard/security/policy-admin/main
 * @author nadir
 *
 */
public class PolicyAdminMainServlet extends RegistrableServlet{

	private static final long serialVersionUID = 1L;
	private String templateName;
	private Template template;
	private IManagementTasks managementTask;
	private Log log = LogFactory.getLog(getClass());
	private IPolicyManager policyManager;
	private Joiner joiner;

	public IPolicyManager getPolicyManager() {
		return policyManager;
	}

	public void setPolicyManager(IPolicyManager policyManager) {
		this.policyManager = policyManager;
	}

	
	public void init()
	{
		template = getVelocityEngineWrapper().getVelocityTemplateByName(templateName);
		joiner = Joiner.on(",");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		List<Entry> listOfTableEntries = new ArrayList<Entry>();
		try {
			Collection<Workspace> collectionOfProjects = managementTask.listWorkspaces();
			
			for(Workspace project : collectionOfProjects)
			{
				for(Profile dataProvider : project.getProfiles().values())
				{
					for(QueryEndpoint queryAPI : dataProvider.getQueryEndpoints().values())
					{
						String resource = String.format("%s/%s/%s/%s", project.getName() , dataProvider.getName() , "query" , queryAPI.getName());
						Set<String> listOfGroups = policyManager.getAuthorizedGroups(resource);
						String authorizedGroups = null;
						if(listOfGroups == null || listOfGroups.size() == 0)
						{
							authorizedGroups = "NONE";
						}
						else
						{
							authorizedGroups = joiner.join(listOfGroups);
						}
						
						Entry entry = new Entry();
						entry.apiName = queryAPI.getName();
						entry.authorizedGroups = authorizedGroups;
						entry.dataProvider = dataProvider.getName();
						entry.project = project.getName();
						entry.type = "query";
						
						listOfTableEntries.add(entry);
						
					}
					
					for(SubmitEndpoint submitAPI : dataProvider.getSubmitEndpoints().values())
					{
						String resource = String.format("%s/%s/%s/%s", project.getName() , dataProvider.getName() , "submit" , submitAPI.getName());
						Set<String> listOfGroups = policyManager.getAuthorizedGroups(resource);
						String authorizedGroups = null;
						if(listOfGroups == null || listOfGroups.size() == 0)
						{
							authorizedGroups = "NONE";
						}
						else
						{
							authorizedGroups = joiner.join(listOfGroups);
						}
						
						Entry entry = new Entry();
						entry.apiName = submitAPI.getName();
						entry.authorizedGroups = authorizedGroups;
						entry.dataProvider = dataProvider.getName();
						entry.project = project.getName();
						entry.type = "submit";
						
						listOfTableEntries.add(entry);
						
					}
					
					for(DeleteEndpoint deleteAPI : dataProvider.getDeleteEndpoints().values())
					{
						String resource = String.format("%s/%s/%s/%s", project.getName() , dataProvider.getName() , "delete" , deleteAPI.getName());
						Set<String> listOfGroups = policyManager.getAuthorizedGroups(resource);
						String authorizedGroups = null;
						if(listOfGroups == null || listOfGroups.size() == 0)
						{
							authorizedGroups = "NONE";
						}
						else
						{
							authorizedGroups = joiner.join(listOfGroups);
						}
						
						Entry entry = new Entry();
						entry.apiName = deleteAPI.getName();
						entry.authorizedGroups = authorizedGroups;
						entry.dataProvider = dataProvider.getName();
						entry.project = project.getName();
						entry.type = "delete";
						
						listOfTableEntries.add(entry);
						
					}
				}
			}
			
			
			VelocityContext context = getVelocityEngineWrapper().createVelocityContext(req);
			context.put("tableEntries", listOfTableEntries);
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

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	


	public static class Entry {
		private String project;
		private String dataProvider;
		private String type;
		private String apiName;
		
		private String authorizedGroups;
		
		public String getProject() {
			return project;
		}
		public void setProject(String project) {
			this.project = project;
		}
		public String getDataProvider() {
			return dataProvider;
		}
		public void setDataProvider(String dataProvider) {
			this.dataProvider = dataProvider;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getApiName() {
			return apiName;
		}
		public void setApiName(String apiName) {
			this.apiName = apiName;
		}
		
		public String getAuthorizedGroups() {
			return authorizedGroups;
		}
		public void setAuthorizedGroups(String authorizedGroups) {
			this.authorizedGroups = authorizedGroups;
		}
		
		
	}
	
	
	

}
