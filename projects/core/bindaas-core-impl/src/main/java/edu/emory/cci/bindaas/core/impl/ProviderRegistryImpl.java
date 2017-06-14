package edu.emory.cci.bindaas.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.api.IProviderRegistry;
import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.core.exception.ProviderNotFoundException;
import edu.emory.cci.bindaas.framework.api.IProvider;

public class ProviderRegistryImpl implements IProviderRegistry{

	private Map<String,Map<Integer,IProvider>> providerRegistry;
	
	
	public void init() throws Exception
	{
		providerRegistry = new HashMap<String, Map<Integer,IProvider>>();
		final BundleContext context = Activator.getContext();
		String filter = "(objectclass=" + IProvider.class.getName() + ")";
		
		ServiceListener providerServiceListener = new ServiceListener() {
			
			@Override
			public void serviceChanged(ServiceEvent sv) {
				ServiceReference<?> serviceRef = sv.getServiceReference();
				      switch(sv.getType()) {
				        case ServiceEvent.REGISTERED :
				          {
				        	  IProvider provider = (IProvider) context.getService(serviceRef);
				        	  registerProvider(provider.getId(), provider.getVersion(), provider);
				        	  break;
				          }
				         
				        case ServiceEvent.UNREGISTERING :
				        {
				        	IProvider provider = (IProvider) context.getService(serviceRef);
				        	unregisterProvider(provider.getId(), provider.getVersion());
				        	break;
				        }
				        
				       default:
				          break;
				     }
				   
				
			}
		};
		
		
		// add existing providers
		@SuppressWarnings("rawtypes")
		ServiceReference[] serviceReferences = context.getAllServiceReferences(IProvider.class.getName(), null);
		if(serviceReferences!=null)
		{
			for(ServiceReference<?> serviceRef : serviceReferences)
			{
				providerServiceListener.serviceChanged( new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
			}
		}
		
		
		
		// listen for new providers
		context.addServiceListener(providerServiceListener, filter);
		//context.registerService(IProviderRegistry.class.getName(), this, null); 
		
	}
	
	@Override
	public void registerProvider(String id, int version, IProvider provider)
			 {
		
		if(providerRegistry.containsKey(id)){
			Map<Integer,IProvider> providers = providerRegistry.get(id);
			providers.put(version, provider);
		}
		else
		{
			Map<Integer,IProvider> providers = new HashMap<Integer, IProvider>();
			providers.put(version, provider);
			providerRegistry.put(id, providers);
		}
		
	}

	@Override
	public IProvider lookupProvider(String id, int version) throws ProviderNotFoundException {
		if(providerRegistry.containsKey(id) && providerRegistry.get(id).containsKey(version)){
			Map<Integer,IProvider> providers = providerRegistry.get(id);
			return providers.get(version);
		}
		else
		{
			throw new ProviderNotFoundException(id, version);
		}
		
	}

	@Override
	public Collection<IProvider> findProvider(String id) {

		return providerRegistry.containsKey(id) ? providerRegistry.get(id).values() : null;
	}

	@Override
	public void unregisterProvider(String id, int version) {

		if(providerRegistry.containsKey(id)){
			Map<Integer,IProvider> providers = providerRegistry.get(id);
			providers.remove(version);
			
		}
		
	}

	@Override
	public Collection<IProvider> findProviders() {
		List<IProvider> listOfAllProvider = new ArrayList<IProvider>();
		
		for(Map<Integer,IProvider> map : providerRegistry.values())
		{
			listOfAllProvider.addAll(map.values());
		}
		return listOfAllProvider;
	}

}
