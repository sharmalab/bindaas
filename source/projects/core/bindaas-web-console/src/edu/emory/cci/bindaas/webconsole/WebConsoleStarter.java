package edu.emory.cci.bindaas.webconsole;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.HttpConstraintElement;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import edu.emory.cci.bindaas.framework.api.IProvider;
import edu.emory.cci.bindaas.webconsole.servlet.usermgmt.PostSignupServlet;
import edu.emory.cci.bindaas.webconsole.servlet.usermgmt.UserRegistrationServlet;

public class WebConsoleStarter {

	// Start the web-console for bindaas
	private MainController mainController;
	private LoginAction loginAction;
	private Log log = LogFactory.getLog(getClass());
	private boolean initialized = false;
	
	
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


	public void init() throws Exception
	{
		Hashtable<String,String> loginFilterprops = new Hashtable<String,String>();
		loginFilterprops.put("pattern", "/dashboard/.*");
		loginFilterprops.put("init.message", "Login Filter");

		String filter = "(objectclass=" + HttpService.class.getName() + ")";
		final BundleContext context = Activator.getContext();
		ServiceListener httpServiceListener = new ServiceListener() {
			
			@Override
			public void serviceChanged(ServiceEvent sv) {
				if(!initialized)
				{

					ServiceReference serviceRef = sv.getServiceReference();
					      switch(sv.getType()) {
					        case ServiceEvent.REGISTERED:
					        case ServiceEvent.MODIFIED:
					          {
					        	  HttpService  service  = (HttpService) context.getService(serviceRef);
					        	  // register
					        	  HttpContext defaultContext = service.createDefaultHttpContext();
									try {
										service.registerResources("/foundation", "/foundation", defaultContext);
										service.registerServlet("/dashboard", mainController, null, defaultContext);
										service.registerServlet("/authenticate", loginAction, null, defaultContext);
										service.registerServlet("/postSignup", new PostSignupServlet(), null, defaultContext);
										service.registerServlet("/userRegistration", new UserRegistrationServlet(), null, defaultContext);
										
										((org.apache.felix.http.api.ExtHttpService) service) .registerFilter(loginAction, "/dashboard/.*", null, 0 ,  defaultContext);
										
										
										OpenIDAuth openIdAuth = new OpenIDAuth();
										service.registerServlet(openIdAuth.getServletLocation(), openIdAuth, null, defaultContext);
										
										CILogonAuth ciLogonAuth = new CILogonAuth();
										service.registerServlet("/cilogon", ciLogonAuth, null, defaultContext);
										
										
									} catch (Exception e) {
											log.error(e);
									}
									
					        	  
					        	  break;
					          }
					         
					        case ServiceEvent.UNREGISTERING :
					        {
					        	// do unregistration;
					        	break;
					        }
					        
					       default:
					          break;
					     }
					      
					initialized = true;
				}
				
			}
		};
		
		
		// add existing
		ServiceReference[] serviceReferences = context.getAllServiceReferences(HttpService.class.getName(), null);
		if(serviceReferences!=null)
		{
			for(ServiceReference serviceRef : serviceReferences)
			{
				httpServiceListener.serviceChanged( new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
				break;
			}
		}
		
		
		
		// listen for new providers
		context.addServiceListener( httpServiceListener , filter);
		
//		ServiceReference ref =  Activator.getContext().getServiceReference(HttpService.class.getName());
//		if(ref!=null)
//		{
//			HttpService service = (HttpService) Activator.getContext().getService(ref);
//			if(service!=null)
//			{
//				try {
//					HttpContext defaultContext = new CustomHttpContext();
//					service.registerResources("/dashboard/foundation", "/foundation", defaultContext);
//					service.registerServlet("/dashboard", mainController, null, defaultContext);
//					service.registerServlet("/authenticate", loginAction, null, defaultContext);
//					((org.apache.felix.http.api.ExtHttpService) service) .registerFilter(loginAction, "/dashboard/.*", null, 0 ,  defaultContext);
//					
//				} catch (NamespaceException e) {
//					log.error(e);
//				}
//			}
//		}
//		else
//		{
//			log.error("HttpService not available. No servlets were registered");
//		}
		
		
	}
	
	
	
	private static class CustomHttpContext implements org.osgi.service.http.HttpContext 
	{

		@Override
		public boolean handleSecurity(HttpServletRequest request,
				HttpServletResponse response) throws IOException {
			
			return true;
		}

		@Override
		public URL getResource(String name) {

			try {
				return new URL(name);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public String getMimeType(String name) {
			 if (name.endsWith(".jpg"))
					  {  
					    return "image/jpeg";  
					  } 
					  else if (name.endsWith(".png")) 
					  {  
					    return "image/png";  
					  } 
					  else 
					  {  
					    return "text/html";  
					  }  
					  
		}
		
	}
	
	
}
