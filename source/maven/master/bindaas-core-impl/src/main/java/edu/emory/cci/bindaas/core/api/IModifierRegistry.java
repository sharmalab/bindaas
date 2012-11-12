package edu.emory.cci.bindaas.core.api;

import java.util.Collection;

import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;

public interface IModifierRegistry {

	public void registerQueryModifier(String id , IQueryModifier queryModifier);
	public void unregisterQueryModifier(String id);
	public IQueryModifier findQueryModifier(String id);
	public Collection<IQueryModifier> findAllQueryModifier();
	
	public void registerQueryResultModifier(String id, IQueryResultModifier queryResultModifier);
	public void unregisterQueryResultModifier(String id);
	public IQueryResultModifier findQueryResultModifier(String id);
	public Collection<IQueryResultModifier> findAllQueryResultModifiers();
	
	public void registerSubmitPayloadModifier(String id, ISubmitPayloadModifier submitPayloadModifier);
	public void unregisterSubmitPayloadModifier(String id);
	public ISubmitPayloadModifier findSubmitPayloadModifier(String id);
	public Collection<ISubmitPayloadModifier> findAllSubmitPayloadModifiers();
	
}
