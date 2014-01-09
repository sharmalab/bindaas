package edu.emory.cci.bindaas.security_dashboard;

import javax.servlet.http.HttpServlet;

import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.util.VelocityEngineWrapper;

public class RegistrableServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String servletPath;
	private SecurityDashboardConfiguration configuration;
	private VelocityEngineWrapper velocityEngineWrapper;
	
	

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public SecurityDashboardConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SecurityDashboardConfiguration configuration) {
		this.configuration = configuration;
	}
	
	

		
}
