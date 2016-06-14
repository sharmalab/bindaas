package edu.emory.cci.bindaas.webconsole.servlet.action;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.security.api.BindaasUser;
import edu.emory.cci.bindaas.webconsole.AbstractRequestHandler;

public class LogoutAction extends AbstractRequestHandler{
	
	private String homepage;
	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}


	private String uriTemplate;
	private Log log = LogFactory.getLog(getClass());
	
	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, Map<String,String> pathParameters) throws Exception {

		if(request.getMethod().equalsIgnoreCase("get"))
		{
			logout(request, response);
		}
		
		else
		{
			throw new Exception("Http Method [" + request.getMethod() + "] not allowed here");
		}
	}
	
	
	private void logout(HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		HttpSession session = request.getSession();
		if(session!=null && session.getAttribute("loggedInUser")!=null)
		{
			BindaasUser admin = (BindaasUser) session.getAttribute("loggedInUser");
			session.invalidate();
			log.debug("Logging out user [" + admin + "]");
		}
		response.sendRedirect(homepage);
	}
	
	



}
