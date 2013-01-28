package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

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
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.api.ISecurityHandler;
import edu.emory.cci.bindaas.core.model.hibernate.HistoryLog;
import edu.emory.cci.bindaas.core.model.hibernate.UserRequest;
import edu.emory.cci.bindaas.core.rest.security.AuthenticationProtocol;
import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.core.util.DynamicProperties;

import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider;
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
	
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String loginTarget = request.getParameter("loginTarget") !=null ? request.getParameter("loginTarget") : defaultLoginTarget;
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		//IAuthenticationProvider authenticationProvider = getAuthenticationProvider() ;
		
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminConsoleConfiguration = Activator.getService(DynamicObject.class, "(name=bindaas.adminconsole)");
		
		
		if(dynamicAdminConsoleConfiguration!=null )
		{
			try {
//				edu.emory.cci.bindaas.security.impl.
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
				
					LoginView.generateLoginView(request , response , loginTarget, "Invalid Username/Password");

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
				LoginView.generateLoginView(httpServletRequest , (HttpServletResponse) response ,  httpServletRequest.getPathInfo(), "You must login to access this resource");
				
			} catch (Exception e) {
				
				ErrorView.handleError((HttpServletResponse) response, e);
			}
			
		}
		
	}

	@Override
	public void init() throws ServletException {
		log.info("LoginAction Servlet  Initialized");
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.info("LoginAction Filter  Initialized");
		
	}
	
//	public IAuthenticationProvider getAuthenticationProvider()
//	{
//		// code for setting authentication provider
//		final BundleContext context = Activator.getContext();
//		DynamicProperties bindaasProperties = Activator.getService(DynamicProperties.class , "(name=bindaas)");
//		String classname = bindaasProperties.get("webconsole.security.method")!=null && bindaasProperties.get("webconsole.security.method").equals("ldap") ? "edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider" : "edu.emory.cci.bindaas.security.impl.FileSystemAuthenticationProvider";
//		ServiceReference[] serviceReferences;
//		try {
//			
//			serviceReferences = context.getAllServiceReferences(IAuthenticationProvider.class.getName(), "(class=" + classname + ")");
//			if(serviceReferences.length > 0)
//			{
//				Object service = context.getService(serviceReferences[0]);
//				if(service!=null)
//				{
//					IAuthenticationProvider authProvider  =  (IAuthenticationProvider) service; 
//					return authProvider; 
//				}
//			}
//		} catch (InvalidSyntaxException e) {
//			log.error(e);
//		}
//		return null;
//	}

	
	

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
