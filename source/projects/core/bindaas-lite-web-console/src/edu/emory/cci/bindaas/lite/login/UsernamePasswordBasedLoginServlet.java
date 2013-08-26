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
import edu.emory.cci.bindaas.lite.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.lite.config.BindaasAdminConsoleConfiguration.AdminConfiguration.AuthenticationMethod;
import edu.emory.cci.bindaas.lite.misc.GeneralServerErrorServlet;
import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.security.api.IAuthenticationProvider;
import edu.emory.cci.bindaas.security.ldap.LDAPAuthenticationProvider;


/**
 * registered at /bindaas/lite/username-password-login
 * @author nadir
 *
 */
public class UsernamePasswordBasedLoginServlet extends RegistrableServlet{

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(getClass());
	
	private GeneralServerErrorServlet errorServlet;
	private LoginServlet loginServlet;
	private PostLogonServlet postLogonServlet;
	private IAuthenticationProvider fileSystemAuthenticationProvider;
	 
	
	public IAuthenticationProvider getFileSystemAuthenticationProvider() {
		return fileSystemAuthenticationProvider;
	}

	public void setFileSystemAuthenticationProvider(
			IAuthenticationProvider fileSystemAuthenticationProvider) {
		this.fileSystemAuthenticationProvider = fileSystemAuthenticationProvider;
	}

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

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try {
			String loginTarget = req.getParameter("loginTarget");
			BindaasUser bindaasUser = authenticate(req , loginTarget);
			if(bindaasUser!=null)
			{
				BindaasSession bindaasSession = new BindaasSession();
				bindaasSession.setBindaasUser(bindaasUser);
				bindaasSession.setLoginTarget(loginTarget);
				HttpSession httpSession = req.getSession(true);
				BindaasSession.setBindaasSession(httpSession, bindaasSession);
				postLogonServlet.redirect(resp);
			}
			else
			{
				loginServlet.redirect(resp, "Invalid Username/Password", loginTarget);
			}
			
		}
		catch(Exception e)
		{
			log.error(e);
			errorServlet.redirect(resp, "Internal Server Error");
		}
	
	}

	private BindaasUser authenticate(HttpServletRequest request,
			String loginTarget) {
		BindaasUser principal = null;
		try {

			String username = request.getParameter("username");
			String password = request.getParameter("password");

			BindaasAdminConsoleConfiguration configuration = getBindaasAdminConfiguration();
			if (configuration.getAdminConfiguration().getAuthenticationMethod()
					.equals(AuthenticationMethod.ldap)) {
				principal = LDAPAuthenticationProvider.login(username,
						password, configuration.getAdminConfiguration()
								.getLdapUrl(), configuration
								.getAdminConfiguration().getLdapDNPattern());
			} else if (configuration.getAdminConfiguration()
					.getAuthenticationMethod()
					.equals(AuthenticationMethod.defaultMethod)) {

				principal = fileSystemAuthenticationProvider.login(username,
						password);
			}
		} catch (Exception e) {
			log.error(e);
		}
		return principal;
	}
}
