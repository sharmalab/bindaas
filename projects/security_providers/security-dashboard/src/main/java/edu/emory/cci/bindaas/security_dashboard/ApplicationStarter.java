package edu.emory.cci.bindaas.security_dashboard;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.security_dashboard.bundle.Activator;
import edu.emory.cci.bindaas.security_dashboard.config.SecurityDashboardConfiguration;
import edu.emory.cci.bindaas.security_dashboard.util.VelocityEngineWrapper;

public class ApplicationStarter {

	private Log log = LogFactory.getLog(getClass());
	private List<RegistrableServlet> registrableServlets;
	private SecurityDashboardConfiguration defaultSecurityDashboardConfiguration;
	private String securityDashboardConfigFileName;
	private VelocityEngineWrapper velocityEngineWrapper;
		

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public String getSecurityDashboardConfigFileName() {
		return securityDashboardConfigFileName;
	}

	public void setSecurityDashboardConfigFileName(
			String securityDashboardConfigFileName) {
		this.securityDashboardConfigFileName = securityDashboardConfigFileName;
	}

	public SecurityDashboardConfiguration getDefaultSecurityDashboardConfiguration() {
		return defaultSecurityDashboardConfiguration;
	}

	public void setDefaultSecurityDashboardConfiguration(
			SecurityDashboardConfiguration defaultSecurityDashboardConfiguration) {
		this.defaultSecurityDashboardConfiguration = defaultSecurityDashboardConfiguration;
	}

	public List<RegistrableServlet> getRegistrableServlets() {
		return registrableServlets;
	}

	public void setRegistrableServlets(List<RegistrableServlet> registrableServlets) {
		this.registrableServlets = registrableServlets;
	}

	
	
	private final static String WEBCONTENT_DIRECTORY=  "/webcontent";
	
	
	public void init() throws Exception
	{
		// register all servlets,filters,etc
		final BundleContext context = Activator.getContext();
		
		// register configuration
		
		DynamicObject<SecurityDashboardConfiguration> dynamicConfig = new DynamicObject<SecurityDashboardConfiguration>(securityDashboardConfigFileName ,defaultSecurityDashboardConfiguration, context);
		
		final SecurityDashboardConfiguration config = dynamicConfig.getObject();
		
		
		ServiceTracker<HttpService,HttpService> serviceTracker = new ServiceTracker<HttpService,HttpService>(context, HttpService.class, new ServiceTrackerCustomizer<HttpService, HttpService>() {

			@Override
			public HttpService addingService(ServiceReference<HttpService> srf) {
				HttpService httpService = context.getService(srf); 
				registerResources(httpService ,  config);
				return httpService;
			}

			@Override
			public void modifiedService(ServiceReference<HttpService> arg0,
					HttpService arg1) {
			}

			@Override
			public void removedService(ServiceReference<HttpService> arg0,
					HttpService arg1) {
				}
		});
		
		serviceTracker.open();
	}
	

	private void registerResources(HttpService httpService , SecurityDashboardConfiguration config) 
	{
		try{
			HttpContext defaultContext = httpService.createDefaultHttpContext();
			for(RegistrableServlet registrableServlet : this.registrableServlets)
			{
				registrableServlet.setConfiguration(config);
				registrableServlet.setVelocityEngineWrapper(velocityEngineWrapper);
				httpService.registerServlet(registrableServlet.getServletPath(), registrableServlet, null, defaultContext);
			}
			
			httpService.registerResources(WEBCONTENT_DIRECTORY, WEBCONTENT_DIRECTORY, defaultContext);
			
		}catch(Exception e)
		{
			log.fatal("ApplicationStarter did not initialize",e);
		}
	}
	
	
	
	
}
