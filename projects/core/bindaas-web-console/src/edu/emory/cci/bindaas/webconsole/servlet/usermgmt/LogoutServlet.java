package edu.emory.cci.bindaas.webconsole.servlet.usermgmt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogoutServlet extends HttpServlet{
	
	
	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(getClass());
	private String loginPage = "/user/login";
	private String servletLocation = "/user/logout";
	private void logout(HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		HttpSession session = request.getSession();
		if(session!=null)
		{
			session.invalidate();
			log.trace("Successfully logged out user");
		}
		response.sendRedirect(loginPage);
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logout(req, resp);
	}
	public String getServletLocation() {
		return servletLocation;
	}
	public void setServletLocation(String servletLocation) {
		this.servletLocation = servletLocation;
	}
	
	



}
