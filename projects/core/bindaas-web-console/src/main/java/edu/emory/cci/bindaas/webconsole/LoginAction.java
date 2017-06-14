package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration.AdminConfiguration.AuthenticationMethod;


public class LoginAction extends HttpServlet implements Filter{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1370274434730700069L;
	private String defaultLoginTarget ;
	private Log log = LogFactory.getLog(getClass());
	private String postLoginActionTarget = "/postAuthenticate";
	private LoginView loginView;
	
	public LoginView getLoginView() {
		return loginView;
	}
	public void setLoginView(LoginView loginView) {
		this.loginView = loginView;
	}




	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String loginTarget = request.getParameter("loginTarget") !=null ? request.getParameter("loginTarget") : defaultLoginTarget;
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		
		
		@SuppressWarnings("unchecked")
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		
		
		if(dynamicAdminConsoleConfiguration!=null )
		{
			try {

				BindaasUser principal = null;
				if(dynamicAdminConsoleConfiguration.getObject().getAdminConfiguration().getAuthenticationMethod().equals(AuthenticationMethod.ldap))
				{
					principal = LDAPAuthenticationProvider.login(username, password , dynamicAdminConsoleConfiguration.getObject().getAdminConfiguration().getLdapUrl() ,dynamicAdminConsoleConfiguration.getObject().getAdminConfiguration().getLdapDNPattern());
				}
				else if(dynamicAdminConsoleConfiguration.getObject().getAdminConfiguration().getAuthenticationMethod().equals(AuthenticationMethod.defaultMethod))
				{
					IAuthenticationProvider authenticationProvider = Activator.getService(IAuthenticationProvider.class , "(class=edu.emory.cci.bindaas.security.impl.FileSystemAuthenticationProvider)");
					principal = authenticationProvider.login(username, password);
				}
				
				if(principal!=null)
				{
					request.getSession(true).setAttribute("loggedInUser", principal);
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
				
					loginView.generateLoginView(request , response , loginTarget, "Invalid Username/Password");

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




	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest  = (HttpServletRequest) request; 
		if(checkIfUserLoggedIn( httpServletRequest)  || ((HttpServletRequest) request).getPathInfo().contains("/foundation") )
		{
			chain.doFilter(request, response);
			
		}
		else
		{
			try {
				loginView.generateLoginView(httpServletRequest , (HttpServletResponse) response ,  httpServletRequest.getPathInfo(), "You must login to access this resource");
				
			} catch (Exception e) {
				
				ErrorView.handleError((HttpServletResponse) response, e);
			}
			
		}
		
	}

	@Override
	public void init() throws ServletException {
		log.trace("LoginAction Servlet  Initialized");
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.trace("LoginAction Filter  Initialized");
		
	}


	private boolean checkIfUserLoggedIn(HttpServletRequest request)
	{
		HttpSession session = request.getSession();
		if(session!=null && session.getAttribute("loggedInUser")!=null)
		{
			return true;
		}
		return false;
	}
}
