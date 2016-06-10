package edu.emory.cci.bindaas.lite.login;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.lite.BindaasSession;

/**
 * Intercepts all requests to access dashboard /bindaas/lite/admin/
 * @author nadir
 *
 */
public class LoginFilter implements Filter{

	private Log log = LogFactory.getLog(getClass());
	private LoginServlet loginServlet;
	private String filterPath;
	
	public LoginServlet getLoginServlet() {
		return loginServlet;
	}

	public void setLoginServlet(LoginServlet loginServlet) {
		this.loginServlet = loginServlet;
	}

	public String getFilterPath() {
		return filterPath;
	}

	public void setFilterPath(String filterPath) {
		this.filterPath = filterPath;
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		
		BindaasSession session = BindaasSession.getBindaasSession(httpServletRequest);
		if(session!=null)
		{
			// proceed to application
			log.trace("session valid. Proceed to application");
			chain.doFilter(request, response);
		}
		else
		{
			// authenticate the user
			log.trace("session invalid. Redirecting user to login page");
			loginServlet.redirect((HttpServletResponse) response, "You must login to access this resource", httpServletRequest.getPathInfo());
		}
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
