package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.cilogon.CILogonHelper;
import edu.emory.cci.bindaas.cilogon.CILogonSession;
import edu.emory.cci.bindaas.openid.OpenIDHelper;
import edu.emory.cci.bindaas.security.api.BindaasUser;

public class CILogonAuth extends HttpServlet {
	
	private String defaultLoginTarget  = "";
	
	private String defaultAttribute2Set = "loggedInUser";
	
	private CILogonSession ciLogonSession;
	
	public String getDefaultLoginTarget() {
		return defaultLoginTarget;
	}

	public void setDefaultLoginTarget(String defaultLoginTarget) {
		this.defaultLoginTarget = defaultLoginTarget;
	}

	private Log log = LogFactory.getLog(getClass());
	
	
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		
		try {
			if (ciLogonSession == null) {
				CILogonHelper cilogonHelper = Activator
						.getService(CILogonHelper.class);
				if (cilogonHelper != null) {
					try {
						ciLogonSession = cilogonHelper.createSession(request
								.getServletPath()); // lazy loading
					} catch (URISyntaxException e) {
						log.error(e);
						throw new ServletException(e);
					}
				} else
					throw new ServletException("CI Logon Service not available");
			}

			ciLogonSession.authRequest(request, response);
		} catch (Exception e) {
			log.error(e);
			ErrorView.handleError(response, e);
		}
		
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		try {
			if (ciLogonSession != null) {
				String loginTarget = request.getParameter("loginTarget") != null ? request
						.getParameter("loginTarget") : defaultLoginTarget;
				String attribute2set = request.getParameter("attribute2set") != null ? request
						.getParameter("attribute2set") : defaultAttribute2Set;
				BindaasUser principal = ciLogonSession.verifyResponse(request);
				if (principal != null) {

					request.getSession(true).setAttribute(attribute2set,
							principal);
					response.sendRedirect(loginTarget);
					// auth success
				} else {
					// auth failed
					log.error("Authentication failed");
					try {

						LoginView.generateLoginView(request, response,
								loginTarget, "Invalid Username/Password");

					} catch (Exception e1) {
						log.error(e1);
						ErrorView.handleError(response, new Exception(
								"Authentication System unavailable"));
					}

				}

			} else {
				throw new ServletException("CI Logon Service not available");
			}
		} catch (Exception e) {
			log.error(e);
			ErrorView.handleError(response, e);
		}
		
	}
	
	public OpenIDHelper getOpenIDHelper()
	{
		// code for setting authentication provider
		final BundleContext context = Activator.getContext();
		ServiceReference[] serviceReferences;
		try {
			serviceReferences = context.getAllServiceReferences(OpenIDHelper.class.getName(), null);
			if(serviceReferences.length > 0)
			{
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
