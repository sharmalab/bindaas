package edu.emory.cci.bindaas.commons.cxf;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.message.Message;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import edu.emory.cci.bindaas.commons.cxf.bundle.Activator;
import edu.emory.cci.bindaas.framework.event.BindaasEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;

public class ResourceScanner implements ServiceListener , EventHandler {

	// map of service.id and their resource contexts
	private Map<String,ResourceContext> resourceContexts;
	private Log log = LogFactory.getLog(getClass());
	public final static String JAX_RS_SERVICE_NAME = "edu.emory.cci.bindaas.commons.cxf.service.name";
	public final static String JAX_RS_SERVICE_ADDRESS = "edu.emory.cci.bindaas.commons.cxf.service.address";
	public final static String JAX_RS_PROVIDER = "edu.emory.cci.bindaas.commons.cxf.provider";
	public final static String JAX_RS_IN_INTERCEPTOR = "edu.emory.cci.bindaas.commons.cxf.in.interceptor";
	public final static String JAX_RS_OUT_INTERCEPTOR = "edu.emory.cci.bindaas.commons.cxf.out.interceptor";
	private BundleContext bundleContext;
	
	public void startScanning() throws InvalidSyntaxException
	{
		resourceContexts = new HashMap<String, ResourceContext>();
		bundleContext = Activator.getContext();
		
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(EventConstants.EVENT_TOPIC, new String[] { BindaasEventConstants.BINDAAS_STOP });
		bundleContext.registerService(EventHandler.class, this, props);
		String filter = String.format("(%s=*)", JAX_RS_SERVICE_NAME);
		bundleContext.addServiceListener(this , filter);
		
		ServiceReference<?> refs[] = bundleContext.getServiceReferences( (String) null , filter);
		if(refs!=null)
		{
			for(ServiceReference<?> ref : refs)
			{
				addResource(ref);
			}	
		}
		
		
		
	}
	
	private synchronized void  addResource(ServiceReference<?> srf)
	{
		Object serviceObj = bundleContext.getService(srf);
		String name = (String) srf.getProperty(JAX_RS_SERVICE_NAME); 
		if(serviceObj!=null && name!=null && !resourceContexts.containsKey(name))
		{
			try {
				
				String publishAddress = (String) srf.getProperty(JAX_RS_SERVICE_ADDRESS); 
				
				Long serviceId =  (Long) srf.getProperty("service.id");
				@SuppressWarnings("unchecked")
				List<RequestHandler> listOfRequestHandlers = (List<RequestHandler>) srf.getProperty(JAX_RS_PROVIDER);
				@SuppressWarnings("unchecked")
				List<Interceptor<? extends Message>> listOfPhaseInInterceptors = (List<Interceptor<? extends Message>>) srf.getProperty(JAX_RS_IN_INTERCEPTOR);
				@SuppressWarnings("unchecked")
				List<Interceptor<? extends Message>> listOfPhaseOutInterceptors = (List<Interceptor<? extends Message>>) srf.getProperty(JAX_RS_OUT_INTERCEPTOR);
				
				JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
				sf.setAddress(publishAddress);
				sf.setServiceBeanObjects(serviceObj);
				
				if(listOfPhaseInInterceptors!=null)
					sf.setInInterceptors(listOfPhaseInInterceptors);
				
				if(listOfPhaseOutInterceptors!=null)
					sf.setOutInterceptors(listOfPhaseOutInterceptors);
				
				if(listOfRequestHandlers!=null)
					sf.setProviders(listOfRequestHandlers);
				
				Server server = sf.create();
				
				ResourceContext resourceContext = new ResourceContext();
				resourceContext.setBundleId(srf.getBundle().getBundleId());
				resourceContext.setServer(server);
				resourceContext.setEndpointUrl(publishAddress);
				resourceContext.setServerStarted(new Date());
				resourceContext.setServiceId(serviceId);
				resourceContext.setName(name);
				resourceContexts.put(name, resourceContext);
				log.info(String.format("Started service [%s] at [%s]", name , publishAddress));
			}
			catch(Exception e)
			{
				log.error("Failed to start the endpoint" , e);
			}
		}
		else
		{
			log.warn("Service not available from Reference. Cannot create ResourceContext");
		}
	}
	
	private synchronized void destroyAll()
	{
		for(ResourceContext context : resourceContexts.values())
		{
			Server server = context.getServer();
			server.destroy();
		}
		
		this.resourceContexts.clear();
	}
	private synchronized void removeResource(ServiceReference<?> srf)
	{
		Object serviceObj = bundleContext.getService(srf);
		if(serviceObj!=null)
		{
		  try {
				
				String name = (String) srf.getProperty(JAX_RS_SERVICE_NAME); 
				ResourceContext context = resourceContexts.remove(name);
				Server server = context.getServer();
				server.destroy();
				log.info(String.format("Removing service [%s] ", name ));
			}
			catch(Exception e)
			{
				log.error("Failed to start the endpoint" , e);
			}
		}
	}

	@Override
	public void serviceChanged(ServiceEvent serviceEvent) {
		
		switch(serviceEvent.getType())
		{
			case ServiceEvent.REGISTERED :
					addResource(serviceEvent.getServiceReference());
					break;
			case ServiceEvent.UNREGISTERING :
					removeResource(serviceEvent.getServiceReference());
		}
	}

	@Override
	public void handleEvent(Event event) {
		if(event instanceof BindaasEvent)
		{
			BindaasEvent bindaasEvent = (BindaasEvent) event;
			if(bindaasEvent.getTopic().equals(BindaasEventConstants.BINDAAS_STOP))
			{
				destroyAll();
			}
		}
		
	}
}
