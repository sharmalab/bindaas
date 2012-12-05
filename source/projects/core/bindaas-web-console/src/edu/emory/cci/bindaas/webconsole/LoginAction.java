package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;
import java.security.Principal;
import java.util.Properties;

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
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.api.ISecurityHandler;

import edu.emory.cci.bindaas.security.api.AuthenticationException;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;


public class LoginAction extends HttpServlet implements Filter{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1370274434730700069L;
	private String defaultLoginTarget ;
	private boolean initialized = false;
	private Log log = LogFactory.getLog(getClass());
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String loginTarget = request.getParameter("loginTarget") !=null ? request.getParameter("loginTarget") : defaultLoginTarget;
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		ISecurityHandler securityHandler = getSecurityHandler();
		IAuthenticationProvider authenticationProvider = securityHandler!=null && securityHandler.locateAuthenticationProvider()!=null ? securityHandler.locateAuthenticationProvider() : null;
		Properties authenticationProviderProps = securityHandler!=null && securityHandler.getAuthenticationProps()!=null ? securityHandler.getAuthenticationProps() : null;
		
		if(authenticationProvider!=null && authenticationProvider.isAuthenticationByUsernamePasswordSupported() && authenticationProviderProps!=null)
		{
			try {
				Principal principal = authenticationProvider.login(username, password, authenticationProviderProps);
				request.getSession(true).setAttribute("loggedInUser", principal);
				response.sendRedirect(loginTarget);
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
				LoginView.generateLoginView(httpServletRequest , (HttpServletResponse) response ,  httpServletRequest.getPathInfo(), "You must login before accessing this resource");
				
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
	
	
	public ISecurityHandler getSecurityHandler()
	{
		// code for setting authentication provider
		final BundleContext context = Activator.getContext();
		ServiceReference[] serviceReferences;
		try {
			serviceReferences = context.getAllServiceReferences(ISecurityHandler.class.getName(), null);
			if(serviceReferences.length > 0)
			{
				Object service = context.getService(serviceReferences[0]);
				if(service!=null)
				{
					ISecurityHandler securityHandler  =  (ISecurityHandler) service; 
					return securityHandler; 
				}
			}
		} catch (InvalidSyntaxException e) {
			log.error(e);
		}
		return null;
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
