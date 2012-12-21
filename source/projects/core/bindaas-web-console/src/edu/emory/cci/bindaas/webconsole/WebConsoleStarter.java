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

public class WebConsoleStarter {

	// Start the web-console for bindaas
	private MainController mainController;
	private LoginAction loginAction;
	private Log log = LogFactory.getLog(getClass());
	
	
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
//		Hashtable<String,String> mainControllerprops = new Hashtable<String,String>();
//		mainControllerprops.put("alias", "/dashboard");
//		mainControllerprops.put("init.message", "Dashboard");
////		mainControllerprops.put("contextId", "web-console");
//		
//		Hashtable<String,String> loginServletprops = new Hashtable<String,String>();
//		loginServletprops.put("alias", "/authenticate");
//		loginServletprops.put("init.message", "Login Servlet");
////		loginServletprops.put("contextId", "web-console");
//		
		Hashtable<String,String> loginFilterprops = new Hashtable<String,String>();
		loginFilterprops.put("pattern", "/dashboard/.*");
		loginFilterprops.put("init.message", "Login Filter");
////		loginFilterprops.put("contextId", "web-console");
//		
//		Hashtable<String,String> servletContextProps = new Hashtable<String,String>();
//		servletContextProps.put("contextId", "web-console");
//
////		Activator.addServiceRegistration( Activator.getContext().registerService(org.osgi.service.http.HttpContext.class.getName(), new CustomHttpContext() , servletContextProps));
//		Activator.addServiceRegistration( Activator.getContext().registerService(Servlet.class.getName(), mainController , mainControllerprops));
//	    Activator.addServiceRegistration( Activator.getContext().registerService(Servlet.class.getName(), loginAction , loginServletprops));
//	    Activator.addServiceRegistration( Activator.getContext().registerService(Filter.class.getName(), loginAction , loginFilterprops));
//	    registerStaticResources();
		
		String filter = "(objectclass=" + HttpService.class.getName() + ")";
		final BundleContext context = Activator.getContext();
		ServiceListener httpServiceListener = new ServiceListener() {
			
			@Override
			public void serviceChanged(ServiceEvent sv) {
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
									
									((org.apache.felix.http.api.ExtHttpService) service) .registerFilter(loginAction, "/dashboard/.*", null, 0 ,  defaultContext);
									
									
									OpenIDAuth openIdAuth = new OpenIDAuth();
									service.registerServlet(openIdAuth.getServletLocation(), openIdAuth, null, defaultContext);
									
									
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
