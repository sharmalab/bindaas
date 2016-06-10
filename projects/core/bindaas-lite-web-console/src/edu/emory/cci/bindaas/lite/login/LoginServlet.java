package edu.emory.cci.bindaas.lite.login;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.lite.RegistrableServlet;
import edu.emory.cci.bindaas.lite.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.lite.util.VelocityEngineWrapper;

/**
 *	registered against /bindaas/lite/login
 *	optional query parameters :
 *		error=<message>
 *		target=<target resource>
 * @author nadir
 *
 */
public class LoginServlet extends RegistrableServlet {

	private static final long serialVersionUID = 1L;
	
	private String defaultTarget; // bindaas/lite/admin/dashboard
	
	private VelocityEngineWrapper velocityWrapper;
	private String templateName; //edu.emory.cci.bindaas.lite.login_loginServlet.html
	private Template template;
	private UsernamePasswordBasedLoginServlet usernamePasswordBasedLoginServlet;
	private OpenIDLoginServlet openIDLoginServlet;
	
	public VelocityEngineWrapper getVelocityWrapper() {
		return velocityWrapper;
	}

	public void setVelocityWrapper(VelocityEngineWrapper velocityWrapper) {
		this.velocityWrapper = velocityWrapper;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public UsernamePasswordBasedLoginServlet getUsernamePasswordBasedLoginServlet() {
		return usernamePasswordBasedLoginServlet;
	}

	public void setUsernamePasswordBasedLoginServlet(
			UsernamePasswordBasedLoginServlet usernamePasswordBasedLoginServlet) {
		this.usernamePasswordBasedLoginServlet = usernamePasswordBasedLoginServlet;
	}

	public OpenIDLoginServlet getOpenIDLoginServlet() {
		return openIDLoginServlet;
	}

	public void setOpenIDLoginServlet(OpenIDLoginServlet openIDLoginServlet) {
		this.openIDLoginServlet = openIDLoginServlet;
	}

	public String getDefaultTarget() {
		return defaultTarget;
	}

	public void setDefaultTarget(String defaultTarget) {
		this.defaultTarget = defaultTarget;
	}

	@Override
	public void init() throws ServletException {
		template = velocityWrapper.getVelocityTemplateByName(templateName);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String error = req.getParameter("error");
		String loginTarget = req.getParameter("loginTarget");
		if(loginTarget == null) loginTarget = defaultTarget;
		
		VelocityContext context = velocityWrapper.createVelocityContext();
		
		if(error!=null)
		context.put("error", error);
		
		BindaasAdminConsoleConfiguration adminconsoleConfiguration = getBindaasAdminConfiguration();
		
		boolean isLdapOrDefault = false;
		if(adminconsoleConfiguration.getAdminConfiguration().getAuthenticationMethod().toString().equals("ldap") || adminconsoleConfiguration.getAdminConfiguration().getAuthenticationMethod().toString().equals("defaultMethod"))
		{
			isLdapOrDefault = true;//$usernamePasswordLoginServletPath
			String usernamePasswordLoginServletPath = usernamePasswordBasedLoginServlet.getServletPath();
			
			context.put("loginTarget", loginTarget);
			context.put("usernamePasswordLoginServletPath", usernamePasswordLoginServletPath);
			
		}
		
		boolean isOpenID = false;
		if(adminconsoleConfiguration.getAdminConfiguration().getAuthenticationMethod().toString().equals("openid"))
		{
			isOpenID = true;
			String googleUrl = String.format("%s?loginTarget=%s&identifier=%s" , openIDLoginServlet.getServletPath() , loginTarget , "https://www.google.com/accounts/o8/id");
			String yahooUrl = String.format("%s?loginTarget=%s&identifier=%s" , openIDLoginServlet.getServletPath() , loginTarget , "https://me.yahoo.com");
			context.put("googleUrl", googleUrl);
			context.put("yahooUrl", yahooUrl);
		}
		
		context.put("isLdapOrDefault" , isLdapOrDefault);
		context.put("isOpenID" , isOpenID);
		context.put("adminconsoleConfiguration" , getBindaasAdminConfiguration());
		template.merge(context, resp.getWriter());
	}

	public void redirect(HttpServletResponse response, String error , String loginTarget) throws IOException
	{
		StringBuilder redirectUrl = new StringBuilder();
		redirectUrl.append(getServletPath()).append("?loginTarget=").append(loginTarget);
		if(error!=null)
		{
			redirectUrl.append("&error=").append(URLEncoder.encode(error,"UTF-8"));
		}
		
		response.sendRedirect(redirectUrl.toString());
	}
}
