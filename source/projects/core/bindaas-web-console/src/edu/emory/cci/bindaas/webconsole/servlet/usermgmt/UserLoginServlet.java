package edu.emory.cci.bindaas.webconsole.servlet.usermgmt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider;
import edu.emory.cci.bindaas.version_manager.api.IVersionManager;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration.UserConfiguration.AuthenticationMethod;
import edu.emory.cci.bindaas.webconsole.util.VelocityEngineWrapper;

public class UserLoginServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public static final String servletLocation = "/user/login";
	private static String userLoginTemplateName = "userLogin.vt";
	private Template userLoginTemplate;
	private String loginTarget = "/user/dashboard/queryBrowser";
	private String postLoginActionTarget = "/user/postAuthenticate";
	private VelocityEngineWrapper velocityEngineWrapper;
	private IVersionManager versionManager;
	public IVersionManager getVersionManager() {
		return versionManager;
	}

	public void setVersionManager(IVersionManager versionManager) {
		this.versionManager = versionManager;
	}

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	
	public void init() 
	{
		userLoginTemplate = velocityEngineWrapper.getVelocityTemplateByName(userLoginTemplateName);
	}
	
	private Log log = LogFactory.getLog(getClass());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		VelocityContext context = new VelocityContext();
		
		/**
		 * Add version information
		 */
		String versionHeader = String.format("System built <strong>%s</strong>  Build date <strong>%s<strong>", versionManager.getSystemBuild() ,versionManager.getSystemBuildDate());;
		context.put("versionHeader", versionHeader);
		
		
		context.put("loginTarget", loginTarget);
		context.put("adminconsoleConfiguration", dynamicAdminConsoleConfiguration.getObject().clone());
		context.put("errorMessage", 
				req.getParameter("errorMessage") !=null ? req.getParameter("errorMessage") : "");
		userLoginTemplate.merge(context, resp.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String loginTarget = request.getParameter("loginTarget") !=null ? request.getParameter("loginTarget") : this.loginTarget;
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		//IAuthenticationProvider authenticationProvider = getAuthenticationProvider() ;
		
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		
		
		if(dynamicAdminConsoleConfiguration!=null )
		{
			try {
//				edu.emory.cci.bindaas.security.impl.
				BindaasUser principal = null;
				if(dynamicAdminConsoleConfiguration.getObject().getUserConfiguration().getAuthenticationMethod().equals(AuthenticationMethod.ldap))
				{
					principal = LDAPAuthenticationProvider.login(username, password , dynamicAdminConsoleConfiguration.getObject().getUserConfiguration().getLdapUrl() ,dynamicAdminConsoleConfiguration.getObject().getUserConfiguration().getLdapDNPattern());
					principal.addProperty(BindaasUser.FIRST_NAME, principal.getName());
					principal.addProperty(BindaasUser.LAST_NAME, principal.getName());
					principal.addProperty(BindaasUser.EMAIL_ADDRESS, principal.getName() + "@" + principal.getDomain());
				}
				else if(dynamicAdminConsoleConfiguration.getObject().getUserConfiguration().getAuthenticationMethod().equals(AuthenticationMethod.none))
				{
					
					principal = new BindaasUser("guest");
					principal.addProperty("apiKey", "none");
					request.getSession(true).setAttribute("userLoggedIn", principal);
					response.sendRedirect(loginTarget);
					return;
				}
				
				if(principal!=null)
				{
					request.getSession(true).setAttribute("userLoggedIn", principal);
					request.getSession().setAttribute("loginTarget", loginTarget);
					response.sendRedirect(postLoginActionTarget);
				}
				else
				{
					ErrorView.handleError(response, new Exception("Autentication Method not supported"));
				}
				
			} catch (AuthenticationException e) {
				log.error(e);
				
				try {
				
					response.sendRedirect(servletLocation + "?errorMessage=Login%20Failed");

				} catch (Exception e1) {
						log.error(e1);
						ErrorView.handleError(response, new Exception("Authentication System unavailable"));
				}
				
			}
		}
		else
		{
			ErrorView.handleError(response, new Exception("Authentication System unavailable"));
		}

	}


	 
	
}
