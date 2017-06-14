package edu.emory.cci.bindaas.security_dashboard.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.apikey.api.IAPIKeyManager;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.security_dashboard.RegistrableServlet;
import edu.emory.cci.bindaas.security_dashboard.api.IPolicyManager;
import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.model.Group;
import edu.emory.cci.bindaas.security_dashboard.model.User;
import edu.emory.cci.bindaas.security_dashboard.util.RakshakUtils;

/**
 *   /dashboard/security/group-admin/view-edit-group
 * @author nadir
 *
 */
public class GroupAdminViewEditGroupServlet extends RegistrableServlet{

	private static final long serialVersionUID = 1L;
	private String templateName;
	private Template template;
	private IManagementTasks managementTask;
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


	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		try{
			
			String content = IOUtils.toString(req.getInputStream());
			SecurityDashboardConfiguration config = getConfiguration();
			Payload payload = GSONUtil.getGSONInstance().fromJson(content, Payload.class);
			RakshakUtils.removeGroup(config, payload.groupName);
			RakshakUtils.addNewGroup(config, payload.listOfUsers, payload.groupName, payload.groupDescription);
			
		}catch(Exception e)
		{
			log.error(e);
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		
		try {
			  
			String groupName = req.getParameter("groupName");
			
			SecurityDashboardConfiguration config = getConfiguration();
			 
			Group group = RakshakUtils.getGroup(config, groupName , apiKeyManager);
			
			Set<User> users = RakshakUtils.getUsersHavingAPIKey(config , apiKeyManager);
			  VelocityContext context = getVelocityEngineWrapper().createVelocityContext(req);
			  
			  Map<String , Set<String>> indexedUserList = new HashMap<String, Set<String>>();
			  for(User u : users)
			  {
				  String key = u.getName().substring(0, 2);
				  if(indexedUserList.containsKey(key)){
					  Set<String> setOfNames =indexedUserList.get(key);
					  setOfNames.add(u.getName());
				  }
				  else
				  {
					  Set<String> setOfNames = new HashSet<String>();
					  setOfNames.add(u.getName());
					  indexedUserList.put(key, setOfNames);
				  }
				  
			  }
			  
			  String jsonized = GSONUtil.getGSONInstance().toJson(indexedUserList);
			
			  
			  
			  context.put("indexedUserList", jsonized);
			  context.put("group", group);
			  
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


	private static class Payload {
		@Expose String groupName;
		@Expose String groupDescription;
		@Expose Set<String> listOfUsers;
	}

}
