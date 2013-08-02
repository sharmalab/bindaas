package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.openid.OpenIDHelper;
import edu.emory.cci.bindaas.security.api.BindaasUser;

public class OpenIDAuth extends HttpServlet {
	
	private static final long serialVersionUID = -3635690241712230630L;
	private String servletLocation = "/openidlogin" ;
	private String defaultLoginTarget  = "/dashboard/";
	private String postLoginActionTarget = "/postAuthenticate";
	
	private String defaultAttribute2Set = "loggedInUser";
	private LoginView loginView;
	private OpenIDHelper openIdHelper;
	
	public OpenIDHelper getOpenIdHelper() {
		return openIdHelper;
	}
	public void setOpenIdHelper(OpenIDHelper openIdHelper) {
		this.openIdHelper = openIdHelper;
	}
	public LoginView getLoginView() {
		return loginView;
	}
	public void setLoginView(LoginView loginView) {
		this.loginView = loginView;
	}
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
		
		String loginTarget = request.getParameter("loginTarget") !=null ? request.getParameter("loginTarget") : defaultLoginTarget;
		String attribute2set = request.getParameter("attribute2set")!=null ? request.getParameter("attribute2set") : defaultAttribute2Set ; 
		if(openIdHelper!=null)
		{
			BindaasUser principal = openIdHelper.verifyResponse(request);
			if(principal!=null)
			{
				request.getSession(true).setAttribute(attribute2set, principal);
				request.getSession().setAttribute("loginTarget", loginTarget);
				response.sendRedirect(postLoginActionTarget);
				// auth success
			}
			else
			{
				// auth failed
				log.error("Authentication failed");
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
			ErrorView.handleError(response, new Exception("OpenID authentication service not available"));
		}
	}
	
	
	
	
}
