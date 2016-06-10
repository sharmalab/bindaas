package edu.emory.cci.bindaas.datasource.provider.genericsql.outputformat;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.datasource.provider.genericsql.bundle.Activator;
import edu.emory.cci.bindaas.datasource.provider.genericsql.model.OutputFormat;




public class OutputFormatRegistry {

	private Map<OutputFormat,IFormatHandler> formatHandlers;
	
	public void init() throws Exception
	{
		formatHandlers = new HashMap<OutputFormat, IFormatHandler>();
		
		final BundleContext context = Activator.getContext();
		String filter = "(objectclass=" + IFormatHandler.class.getName() + ")"; 
		ServiceListener serviceListener = new ServiceListener() {
			
			@Override
			public void serviceChanged(ServiceEvent sv) {
				ServiceReference<?> serviceRef = sv.getServiceReference();
				      switch(sv.getType()) {
				        case ServiceEvent.REGISTERED :
				          {
				        	  IFormatHandler handler = (IFormatHandler) context.getService(serviceRef);
				        	  formatHandlers.put(handler.getOutputFormat(), handler);
				        	  break;
				          }
				         
				        case ServiceEvent.UNREGISTERING :
				        {
				        	IFormatHandler handler = (IFormatHandler) context.getService(serviceRef);
				        	formatHandlers.remove(handler.getOutputFormat());
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
			for(ServiceReference<?> serviceRef : serviceReferences)
			{
				serviceListener.serviceChanged( new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
			}
		}
		
		
		
		// listen for new providers
		context.addServiceListener(serviceListener, filter);
		context.registerService(OutputFormatRegistry.class.getName(), this, null);
	}
	
	
	public IFormatHandler getHandler(OutputFormat outputFormat)
	{
		return formatHandlers.get(outputFormat);
	}
}
