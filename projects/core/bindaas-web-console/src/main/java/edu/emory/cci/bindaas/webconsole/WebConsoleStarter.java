package edu.emory.cci.bindaas.webconsole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import edu.emory.cci.bindaas.core.util.DynamicObject;
import edu.emory.cci.bindaas.webconsole.bundle.Activator;
import edu.emory.cci.bindaas.webconsole.config.BindaasAdminConsoleConfiguration;
import edu.emory.cci.bindaas.webconsole.servlet.usermgmt.LogoutServlet;
import edu.emory.cci.bindaas.webconsole.servlet.usermgmt.PostUserLoginServlet;
import edu.emory.cci.bindaas.webconsole.servlet.usermgmt.UserLoginServlet;
import edu.emory.cci.bindaas.webconsole.servlet.usermgmt.UserOpenIDAuthServlet;
import edu.emory.cci.bindaas.webconsole.servlet.usermgmt.UserQueryBrowserServlet;
import edu.emory.cci.bindaas.webconsole.servlet.util.DocumentationFetcherServlet;

public class WebConsoleStarter {

	public PostLoginAction getPostLoginAction() {
		return postLoginAction;
	}

	public void setPostLoginAction(PostLoginAction postLoginAction) {
		this.postLoginAction = postLoginAction;
	}

	// Start the web-console for bindaas
	private MainController mainController;
	private LoginAction loginAction;
	private PostLoginAction postLoginAction;
	private Log log = LogFactory.getLog(getClass());
	private boolean initialized = false;
	private DynamicObject<BindaasAdminConsoleConfiguration> bindaasAdminConsoleConfiguration;
	private BindaasAdminConsoleConfiguration defaultBindaasAdminConsoleConfiguration;
	private OpenIDAuth openIdAuth;
	private UserQueryBrowserServlet userQueryBrowserServlet;
	private UserOpenIDAuthServlet userOpenIDAuthServlet;
	private PostUserLoginServlet postUserLoginServlet;
	private LogoutServlet logoutServlet;
	private UserLoginServlet userLoginServlet;

	public UserQueryBrowserServlet getUserQueryBrowserServlet() {
		return userQueryBrowserServlet;
	}

	public void setUserQueryBrowserServlet(
			UserQueryBrowserServlet userQueryBrowserServlet) {
		this.userQueryBrowserServlet = userQueryBrowserServlet;
	}

	public UserOpenIDAuthServlet getUserOpenIDAuthServlet() {
		return userOpenIDAuthServlet;
	}

	public void setUserOpenIDAuthServlet(
			UserOpenIDAuthServlet userOpenIDAuthServlet) {
		this.userOpenIDAuthServlet = userOpenIDAuthServlet;
	}

	public PostUserLoginServlet getPostUserLoginServlet() {
		return postUserLoginServlet;
	}

	public void setPostUserLoginServlet(
			PostUserLoginServlet postUserLoginServlet) {
		this.postUserLoginServlet = postUserLoginServlet;
	}

	public LogoutServlet getLogoutServlet() {
		return logoutServlet;
	}

	public void setLogoutServlet(LogoutServlet logoutServlet) {
		this.logoutServlet = logoutServlet;
	}

	public UserLoginServlet getUserLoginServlet() {
		return userLoginServlet;
	}

	public void setUserLoginServlet(UserLoginServlet userLoginServlet) {
		this.userLoginServlet = userLoginServlet;
	}

	public OpenIDAuth getOpenIdAuth() {
		return openIdAuth;
	}

	public void setOpenIdAuth(OpenIDAuth openIdAuth) {
		this.openIdAuth = openIdAuth;
	}

	public DocumentationFetcherServlet getDocumentationFetcher() {
		return documentationFetcher;
	}

	public void setDocumentationFetcher(
			DocumentationFetcherServlet documentationFetcher) {
		this.documentationFetcher = documentationFetcher;
	}

	private DocumentationFetcherServlet documentationFetcher;

	public BindaasAdminConsoleConfiguration getDefaultBindaasAdminConsoleConfiguration() {
		return defaultBindaasAdminConsoleConfiguration;
	}

	public void setDefaultBindaasAdminConsoleConfiguration(
			BindaasAdminConsoleConfiguration defaultBindaasAdminConsoleConfiguration) {
		this.defaultBindaasAdminConsoleConfiguration = defaultBindaasAdminConsoleConfiguration;
	}

	public MainController getMainController() {
		return mainController;
	}

	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

	public LoginAction getLoginAction() {
		return loginAction;
	}

	public void setLoginAction(LoginAction loginAction) {
		this.loginAction = loginAction;
	}

	private void updateBindaasAdminConfig(DynamicObject<BindaasAdminConsoleConfiguration> bindaasAdminConsoleConfiguration)
	{
		String port = System.getProperty("org.osgi.service.http.port"); // TODO : need to find a better way of changing port
		if(port!=null)
		{
			try{
				
				bindaasAdminConsoleConfiguration.getObject().setPort(Integer.parseInt(port));
				bindaasAdminConsoleConfiguration.saveObject();
				
			}catch(Exception e)
			{
				log.error("Failed to override webconsole port. Using default",e);
			}
		}
	}
	
	public void init() throws Exception {
		log.trace("Initializing bean WebConsoleStarter");
		final BundleContext context = Activator.getContext();
		// set config
		bindaasAdminConsoleConfiguration = new DynamicObject<BindaasAdminConsoleConfiguration>(
				"bindaas.adminconsole",
				defaultBindaasAdminConsoleConfiguration, context);
	
		updateBindaasAdminConfig(bindaasAdminConsoleConfiguration);
		
		String filter = "(objectclass=" + HttpService.class.getName() + ")";

		ServiceListener httpServiceListener = new ServiceListener() {

			@Override
			public void serviceChanged(ServiceEvent sv) {
				

					ServiceReference<?> serviceRef = sv.getServiceReference();
					switch (sv.getType()) {
					case ServiceEvent.REGISTERED:
					case ServiceEvent.MODIFIED:
					if (!initialized) 
					{
						log.trace("Requesting reference of HttpService");
						HttpService service = (HttpService) context
								.getService(serviceRef);
						// register
						HttpContext defaultContext = service
								.createDefaultHttpContext();
						try {
							service.registerResources("/foundation",
									"/foundation", defaultContext);
							service.registerServlet("/dashboard",
									mainController, null, defaultContext);
							service.registerServlet("/authenticate",
									loginAction, null, defaultContext);
							service.registerServlet("/postAuthenticate",
									postLoginAction, null, defaultContext);

							((org.apache.felix.http.api.ExtHttpService) service)
									.registerFilter(loginAction,
											"/dashboard/.*", null, 0,
											defaultContext);
							service.registerServlet(
									openIdAuth.getServletLocation(),
									openIdAuth, null, defaultContext);

							service.registerServlet("/fetchDocumentation",
									documentationFetcher, null, defaultContext);

							service.registerServlet("/user/login",
									userLoginServlet, null, defaultContext);
							service.registerServlet(
									"/user/dashboard/queryBrowser",
									userQueryBrowserServlet, null,
									defaultContext);
							service.registerServlet("/user/openid",
									userOpenIDAuthServlet, null, defaultContext);
							service.registerServlet("/user/postAuthenticate",
									postUserLoginServlet, null, defaultContext);
							service.registerServlet("/user/logout",
									logoutServlet, null, defaultContext);

							log.info("WebConsole Started");
							initialized = true;
						} catch (Exception e) {
							log.error(e);
						}

						break;
					}

					case ServiceEvent.UNREGISTERING: {
						// do unregistration;
						{
							log.trace("Requesting reference of HttpService");
							org.apache.felix.http.api.ExtHttpService service = (org.apache.felix.http.api.ExtHttpService) context
									.getService(serviceRef);
							
							try {
								service.unregister("/foundation");
								service.unregister("/dashboard");
								service.unregister("/authenticate");
								service.unregister("/postAuthenticate");

								service.unregisterFilter(loginAction);
								service.unregister(openIdAuth
										.getServletLocation());

								service.unregister("/fetchDocumentation");

								service.unregister("/user/login");
								service.unregister("/user/dashboard/queryBrowser");
								service.unregister("/user/openid");
								service.unregister("/user/postAuthenticate");
								service.unregister("/user/logout");

								log.trace("Bindaas WebConsole Stopped");
							} catch (Exception e) {
								log.error(e);
							}

							break;
						}

					}

					default:
						break;
					}

					
				}

			
		};

		//

		log.trace("Finding all existing reference of HttpService");

		// listen for new providers
		log.trace("Listening on HttpService");
		context.addServiceListener(httpServiceListener, filter);

		// add existing
		@SuppressWarnings("rawtypes")
		ServiceReference[] serviceReferences = context.getAllServiceReferences(
				HttpService.class.getName(), null);
		if (serviceReferences != null) {
			for (@SuppressWarnings("rawtypes")
			ServiceReference serviceRef : serviceReferences) {
				httpServiceListener.serviceChanged(new ServiceEvent(
						ServiceEvent.REGISTERED, serviceRef));
				break;
			}
		}

	}

}
