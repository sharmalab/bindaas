package edu.emory.cci.bindaas.webconsole.servlet.usermgmt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.openid.OpenIDHelper;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.ErrorView;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;

public class UserOpenIDAuthServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String servletLocation = "/user/openidlogin" ;
	private String defaultLoginTarget  = "/user/login";
	private String postLoginActionTarget = "/user/postAuthenticate";
	
	
	public String getDefaultLoginTarget() {
		return defaultLoginTarget;
	}

	public void setDefaultLoginTarget(String defaultLoginTarget) {
		this.defaultLoginTarget = defaultLoginTarget;
	}

	private Log log = LogFactory.getLog(getClass());
	
	public String getServletLocation() {
		return servletLocation;
	}

	public void setServletLocation(String servletLocation) {
		this.servletLocation = servletLocation;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		OpenIDHelper openIdHelper = getOpenIDHelper();
		if(openIdHelper!=null)
		{
			openIdHelper.authRequest(request, response, servletLocation);
		}
		else
		{
			ErrorView.handleError(response, new Exception("OpenID authentication service not available"));
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		OpenIDHelper openIdHelper = getOpenIDHelper();
		String loginTarget = request.getParameter("loginTarget") !=null ? request.getParameter("loginTarget") : defaultLoginTarget;
		 
		if(openIdHelper!=null)
		{
			BindaasUser principal = openIdHelper.verifyResponse(request);
			if(principal!=null)
			{
				request.getSession(true).setAttribute("userLoggedIn", principal);
				request.getSession().setAttribute("loginTarget", loginTarget);
				response.sendRedirect(postLoginActionTarget);
				
			}
			else
			{
				// auth failed
				log.error("Authentication failed");
				try {
					
					response.sendRedirect(defaultLoginTarget);

				} catch (Exception e1) {
						log.error(e1);
						ErrorView.handleError(response, new Exception("Authentication System unavailable"));
				}


			}
		}
		else
		{
			ErrorView.handleError(response, new Exception("OpenID authentication service not available"));
		}
	}
	
	public OpenIDHelper getOpenIDHelper()
	{
		// code for setting authentication provider
		final BundleContext context = Activator.getContext();
		@SuppressWarnings("rawtypes")
		ServiceReference[] serviceReferences;
		try {
			serviceReferences = context.getAllServiceReferences(OpenIDHelper.class.getName(), null);
			if(serviceReferences.length > 0)
			{
				@SuppressWarnings("unchecked")
				Object service = context.getService(serviceReferences[0]);
				if(service!=null)
				{
					OpenIDHelper openIdHelper  =  (OpenIDHelper) service; 
					return openIdHelper; 
				}
			}
		} catch (InvalidSyntaxException e) {
			log.error(e);
		}
		return null;
	}
	
	
}
