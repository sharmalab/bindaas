package edu.emory.cci.bindaas.core.api;

import java.util.Collection;

import edu.emory.cci.bindaas.core.exception.ProviderNotFoundException;
import edu.emory.cci.bindaas.framework.api.IProvider;

public interface IProviderRegistry {

	public void registerProvider(String id , int version , IProvider provider);
	public IProvider lookupProvider(String id, int version) throws ProviderNotFoundException;
	public Collection<IProvider> findProvider(String id) ;
	public void unregisterProvider(String id , int version);
	public Collection<IProvider> findProviders();
	
	
}
