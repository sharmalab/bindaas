package edu.emory.cci.bindaas.datasource.provider.aime4.outputformat;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.datasource.provider.aime4.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.OutputFormat;
import edu.emory.cci.bindaas.datasource.provider.aime4.model.OutputFormatProps.QueryType;



public class OutputFormatRegistry {

	private Map<String,IFormatHandler> formatHandlers;
	
	public void init() throws Exception
	{
		formatHandlers = new HashMap<String, IFormatHandler>();
		
		final BundleContext context = Activator.getContext();
		String filter = "(objectclass=" + IFormatHandler.class.getName() + ")"; 
		ServiceListener serviceListener = new ServiceListener() {
			
			@Override
			public void serviceChanged(ServiceEvent sv) {
				@SuppressWarnings("rawtypes")
				ServiceReference serviceRef = sv.getServiceReference();
				      switch(sv.getType()) {
				        case ServiceEvent.REGISTERED :
				          {
				        	  @SuppressWarnings("unchecked")
				        	  IFormatHandler handler = (IFormatHandler) context.getService(serviceRef);
				        	  formatHandlers.put(handler.getQueryType() + "|"  + handler.getOutputFormat(), handler);
				        	  break;
				          }
				         
				        case ServiceEvent.UNREGISTERING :
				        {
				        	@SuppressWarnings("unchecked")
				        	IFormatHandler handler = (IFormatHandler) context.getService(serviceRef);
				        	formatHandlers.remove(handler.getQueryType() + "|"  + handler.getOutputFormat());
				        	break;
				        }
				        
				       default:
				          break;
				     }
				   
				
			}
		};
		
		
		// add existing providers
		@SuppressWarnings("rawtypes")
		ServiceReference[] serviceReferences = context.getAllServiceReferences(IFormatHandler.class.getName(), null);
		if(serviceReferences!=null)
		{
			for(@SuppressWarnings("rawtypes") ServiceReference serviceRef : serviceReferences)
			{
				serviceListener.serviceChanged( new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
			}
		}
		
		
		
		// listen for new providers
		context.addServiceListener(serviceListener, filter);
	}
	
	
	public IFormatHandler getHandler(QueryType queryType , OutputFormat outputFormat)
	{
		return formatHandlers.get(queryType + "|" + outputFormat);
	}
}
