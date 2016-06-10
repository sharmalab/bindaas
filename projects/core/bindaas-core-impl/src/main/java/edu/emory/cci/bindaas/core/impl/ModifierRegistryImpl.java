package edu.emory.cci.bindaas.core.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.core.bundle.Activator;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;

public class ModifierRegistryImpl implements IModifierRegistry{

	private Map<String,IQueryModifier> queryModifierRegistry;
	private Map<String, IQueryResultModifier> queryResultModifierRegistry;
	private Map<String,ISubmitPayloadModifier> submitPayloadModifierRegistry;
	
	public void init() throws Exception
	{
		queryModifierRegistry = new HashMap<String, IQueryModifier>();
		queryResultModifierRegistry = new HashMap<String, IQueryResultModifier>();
		submitPayloadModifierRegistry = new HashMap<String, ISubmitPayloadModifier>();
		
		
		new RegistryTracker<IQueryModifier>(queryModifierRegistry, IQueryModifier.class.getName());
		new RegistryTracker<IQueryResultModifier>(queryResultModifierRegistry, IQueryResultModifier.class.getName());
		new RegistryTracker<ISubmitPayloadModifier>(submitPayloadModifierRegistry, ISubmitPayloadModifier.class.getName());
//		Activator.getContext().registerService(IModifierRegistry.class.getName(), this, null);
	}
	
	public Map<String, IQueryModifier> getQueryModifierRegistry() {
		return queryModifierRegistry;
	}

	public void setQueryModifierRegistry(
			Map<String, IQueryModifier> queryModifierRegistry) {
		this.queryModifierRegistry = queryModifierRegistry;
	}

	public Map<String, IQueryResultModifier> getQueryResultModifierRegistry() {
		return queryResultModifierRegistry;
	}

	public void setQueryResultModifierRegistry(
			Map<String, IQueryResultModifier> queryResultModifierRegistry) {
		this.queryResultModifierRegistry = queryResultModifierRegistry;
	}

	public Map<String, ISubmitPayloadModifier> getSubmitPayloadModifierRegistry() {
		return submitPayloadModifierRegistry;
	}

	public void setSubmitPayloadModifierRegistry(
			Map<String, ISubmitPayloadModifier> submitPayloadModifierRegistry) {
		this.submitPayloadModifierRegistry = submitPayloadModifierRegistry;
	}

	@Override
	public void registerQueryModifier(String id, IQueryModifier queryModifier) {
		queryModifierRegistry.put(id, queryModifier);
		
	}

	@Override
	public void unregisterQueryModifier(String id) {
		queryModifierRegistry.remove(id);
		
	}

	@Override
	public IQueryModifier findQueryModifier(String id) {
		
		return queryModifierRegistry.get(id);
	}

	@Override
	public void registerQueryResultModifier(String id,
			IQueryResultModifier queryResultModifier) {
		queryResultModifierRegistry.put(id, queryResultModifier);
		
	}

	@Override
	public void unregisterQueryResultModifier(String id) {
		queryResultModifierRegistry.remove(id);
		
	}

	@Override
	public IQueryResultModifier findQueryResultModifier(String id) {

		return queryResultModifierRegistry.get(id);
	}

	@Override
	public void registerSubmitPayloadModifier(String id,
			ISubmitPayloadModifier submitPayloadModifier) {
		submitPayloadModifierRegistry.put(id, submitPayloadModifier);
		
	}

	@Override
	public void unregisterSubmitPayloadModifier(String id) {
		submitPayloadModifierRegistry.remove(id);
		
	}

	@Override
	public ISubmitPayloadModifier findSubmitPayloadModifier(String id) {

		return submitPayloadModifierRegistry.get(id);
	}

	@Override
	public Collection<IQueryModifier> findAllQueryModifier() {
		
		return queryModifierRegistry.values();
	}

	@Override
	public Collection<IQueryResultModifier> findAllQueryResultModifiers() {

		return queryResultModifierRegistry.values();
	}

	@Override
	public Collection<ISubmitPayloadModifier> findAllSubmitPayloadModifiers() {
		return submitPayloadModifierRegistry.values();
	}

	private static class RegistryTracker<T> {
		
		public RegistryTracker(final Map<String,T> registry , String objectClass) throws Exception
		{
			
			final BundleContext context = Activator.getContext();
			String filter = "(objectclass=" + objectClass + ")"; 
			ServiceListener serviceListener = new ServiceListener() {
				
				@Override
				public void serviceChanged(ServiceEvent sv) {
					ServiceReference<?> serviceRef = sv.getServiceReference();
					      switch(sv.getType()) {
					        case ServiceEvent.REGISTERED :
					          {
					        	  @SuppressWarnings("unchecked")
								T serv = (T) context.getService(serviceRef);
					        	  registry.put(serv.getClass().getName(), serv);
					        	  break;
					          }
					         
					        case ServiceEvent.UNREGISTERING :
					        {
					        	 @SuppressWarnings("unchecked")
					        	T serv = (T) context.getService(serviceRef);
					        	registry.remove(serv.getClass().getName());
					        	break;
					        }
					        
					       default:
					          break;
					     }
					   
					
				}
			};
			
			
			// add existing providers
			@SuppressWarnings("rawtypes")
			ServiceReference[] serviceReferences = context.getAllServiceReferences(objectClass, null);
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
	}

}
