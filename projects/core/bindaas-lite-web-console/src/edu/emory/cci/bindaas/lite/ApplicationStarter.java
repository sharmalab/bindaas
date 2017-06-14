package edu.emory.cci.bindaas.lite;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.lite.bundle.Activator;
import edu.emory.cci.bindaas.lite.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.lite.login.LoginFilter;

public class ApplicationStarter {

	private Log log = LogFactory.getLog(getClass());
	private List<RegistrableServlet> registrableServlets;
	private LoginFilter loginFilter;
	
	public List<RegistrableServlet> getRegistrableServlets() {
		return registrableServlets;
	}

	public void setRegistrableServlets(List<RegistrableServlet> registrableServlets) {
		this.registrableServlets = registrableServlets;
	}

	public LoginFilter getLoginFilter() {
		return loginFilter;
	}

	public void setLoginFilter(LoginFilter loginFilter) {
		this.loginFilter = loginFilter;
	}

	public BindaasAdminConsoleConfiguration getDefaultAdminconsoleConfiguration() {
		return defaultAdminconsoleConfiguration;
	}

	public void setDefaultAdminconsoleConfiguration(
			BindaasAdminConsoleConfiguration defaultAdminconsoleConfiguration) {
		this.defaultAdminconsoleConfiguration = defaultAdminconsoleConfiguration;
	}

	private final static String WEBCONTENT_DIRECTORY=  "/webcontent";
	private BindaasAdminConsoleConfiguration defaultAdminconsoleConfiguration;
	
	public void init() throws Exception
	{
		// register all servlets,filters,etc
		final BundleContext context = Activator.getContext();
		ServiceTracker<HttpService,HttpService> serviceTracker = new ServiceTracker<HttpService,HttpService>(context, HttpService.class, new ServiceTrackerCustomizer<HttpService, HttpService>() {

			@Override
			public HttpService addingService(ServiceReference<HttpService> srf) {
				HttpService httpService = context.getService(srf); 
				registerResources(httpService);
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
		
		DynamicObject<BindaasAdminConsoleConfiguration> dynamicObject = new DynamicObject<BindaasAdminConsoleConfiguration>("bindaas-lite-adminconsole", this.defaultAdminconsoleConfiguration	, context);
		subscribeConfiguration(dynamicObject);
	}
	
	private void subscribeConfiguration(DynamicObject<BindaasAdminConsoleConfiguration> dynamicAdminconsoleConfiguration)
	{
		for(RegistrableServlet registrableServlet : this.registrableServlets)
		{
			BindaasAdminConsoleConfiguration adminconsoleConfiguration = dynamicAdminconsoleConfiguration.addChangeListener(registrableServlet);
			registrableServlet.setBindaasAdminConfiguration(adminconsoleConfiguration);
		}
	}
	
	private void registerResources(HttpService httpService) 
	{
		try{
			HttpContext defaultContext = httpService.createDefaultHttpContext();
			for(RegistrableServlet registrableServlet : this.registrableServlets)
			{
				httpService.registerServlet(registrableServlet.getServletPath(), registrableServlet, null, defaultContext);
			}
			
			ExtHttpService.class.cast(httpService).registerFilter(loginFilter, loginFilter.getFilterPath(),  null, 0, defaultContext);
			httpService.registerResources(WEBCONTENT_DIRECTORY, WEBCONTENT_DIRECTORY, defaultContext);
			
		}catch(Exception e)
		{
			log.fatal("ApplicationStarter did not initialize",e);
		}
	}
	
}
