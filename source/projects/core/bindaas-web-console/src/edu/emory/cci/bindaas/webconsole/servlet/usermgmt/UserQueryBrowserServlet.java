package edu.emory.cci.bindaas.webconsole.servlet.usermgmt;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.api.IManagementTasks;
import edu.emory.cci.bindaas.core.config.BindaasConfiguration;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.framework.model.Workspace;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.Activator;
import edu.emory.cci.bindaas.webconsole.ErrorView;

public class UserQueryBrowserServlet extends HttpServlet {
	public static final String servletLocation = "/user/dashboard/queryBrowser";
	private static String userQueryBrowserTemplateName = "userQueryBrowser.vt";
	private String loginPage = "/user/login";
	
	private static Template userQueryBrowserTemplate;
	
	static {
		userQueryBrowserTemplate = Activator.getVelocityTemplateByName(userQueryBrowserTemplateName);
		
	}
	
	private Log log = LogFactory.getLog(getClass());
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if(request.getSession().getAttribute("userLoggedIn") == null)
		{
			response.sendRedirect(loginPage);
		}
		try{
			IManagementTasks managementTasks = Activator.getService(IManagementTasks.class);
			DynamicObject<BindaasConfiguration> bindaasConfiguration = Activator.getService(DynamicObject.class , "(name=bindaas)");
			if(managementTasks!=null && bindaasConfiguration!=null)
			{
				Collection<Workspace> workspaces = managementTasks.listWorkspaces();
				VelocityContext context = new VelocityContext();
				context.put("workspaces", workspaces);
				context.put("bindaasUser" , BindaasUser.class.cast(request.getSession().getAttribute("userLoggedIn")).getName());
				
				String serviceUrl = bindaasConfiguration.getObject().getProxyUrl();
				context.put("serviceUrl", serviceUrl);
				
				BindaasUser user = (BindaasUser) request.getSession().getAttribute("userLoggedIn");
				context.put("apiKey", user.getProperty("apiKey"));
				userQueryBrowserTemplate.merge(context, response.getWriter());
			}
			else
			{
				log.error("IManagementTasks and/or IBindaasAdminService service not available");
				throw new Exception("Service not available");
			}

		}
		catch(Exception e)
		{
			log.error(e);
			ErrorView.handleError(response, e);
		}
		
		
	}


	 
	
}
