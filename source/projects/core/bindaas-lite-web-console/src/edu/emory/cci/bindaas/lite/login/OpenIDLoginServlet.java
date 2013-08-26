package edu.emory.cci.bindaas.lite.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.lite.BindaasSession;
import edu.emory.cci.bindaas.lite.RegistrableServlet;
import edu.emory.cci.bindaas.lite.misc.GeneralServerErrorServlet;
import edu.emory.cci.bindaas.openid.OpenIDHelper;
import edu.emory.cci.bindaas.security.api.BindaasUser;


/**
 * registered at /bindaas/lite/openid-login
 * 	Mandatory parameters:
 * 			identifier - idenitifying the OpenID provider
 * @author nadir
 *
 */

public class OpenIDLoginServlet extends RegistrableServlet {
	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(getClass());
	private OpenIDHelper openIdHelper;
	private GeneralServerErrorServlet errorServlet;
	private LoginServlet loginServlet;
	private PostLogonServlet postLogonServlet;
	 
	
	
	public GeneralServerErrorServlet getErrorServlet() {
		return errorServlet;
	}
	public void setErrorServlet(GeneralServerErrorServlet errorServlet) {
		this.errorServlet = errorServlet;
	}
	public LoginServlet getLoginServlet() {
		return loginServlet;
	}
	public void setLoginServlet(LoginServlet loginServlet) {
		this.loginServlet = loginServlet;
	}
	public PostLogonServlet getPostLogonServlet() {
		return postLogonServlet;
	}
	public void setPostLogonServlet(PostLogonServlet postLogonServlet) {
		this.postLogonServlet = postLogonServlet;
	}
	
	public OpenIDHelper getOpenIdHelper() {
		return openIdHelper;
	}
	public void setOpenIdHelper(OpenIDHelper openIdHelper) {
		this.openIdHelper = openIdHelper;
	}	
	

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if(openIdHelper!=null)
		{
			openIdHelper.authRequest(request, response, getServletPath());
		}
		else
		{
			log.error("OpenIDHelper not set");
			errorServlet.redirect(response, "Internal Server Error");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String loginTarget = request.getParameter("loginTarget") !=null ? request.getParameter("loginTarget") : loginServlet.getDefaultTarget();
		 
		if(openIdHelper!=null)
		{
			BindaasUser principal = openIdHelper.verifyResponse(request);
			if(principal!=null)
			{
				BindaasSession bindaasSession = new BindaasSession();
				bindaasSession.setBindaasUser(principal);
				bindaasSession.setLoginTarget(loginTarget);
				
				HttpSession httpSession = request.getSession(true);
				BindaasSession.setBindaasSession(httpSession, bindaasSession);
				postLogonServlet.redirect(response);
				// auth success
			}
			else
			{
				// auth failed
				log.error("Authentication failed");
				try {
					loginServlet.redirect(response, "You could not be authenticated by the OpenID provider", loginTarget);

				} catch (Exception e1) {
						log.error(e1);
						errorServlet.redirect(response, "Authentication System unavailable");
				}


			}
		}
		else
		{
			errorServlet.redirect(response, "OpenID authentication service not available");
		}
	}
	
		

}
