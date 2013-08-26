package edu.emory.cci.bindaas.lite;

import javax.servlet.http.HttpServlet;

import edu.emory.cci.bindaas.core.util.DynamicObject.DynamicObjectChangeListener;
import edu.emory.cci.bindaas.lite.config.BindaasAdminConsoleConfiguration;

public class RegistrableServlet extends HttpServlet implements DynamicObjectChangeListener<BindaasAdminConsoleConfiguration> {

	private static final long serialVersionUID = 1L;
	private String servletPath;
	private BindaasAdminConsoleConfiguration bindaasAdminConfiguration;

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	@Override
	public void update(BindaasAdminConsoleConfiguration object) {
			this.bindaasAdminConfiguration = object;
	}

	public BindaasAdminConsoleConfiguration getBindaasAdminConfiguration() {
		return bindaasAdminConfiguration;
	}

	public void setBindaasAdminConfiguration(
			BindaasAdminConsoleConfiguration bindaasAdminConfiguration) {
		this.bindaasAdminConfiguration = bindaasAdminConfiguration;
	}
	
	
	
}
