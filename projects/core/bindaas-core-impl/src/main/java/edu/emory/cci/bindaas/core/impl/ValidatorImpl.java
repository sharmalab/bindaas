package edu.emory.cci.bindaas.core.impl;

import edu.emory.cci.bindaas.core.api.IModifierRegistry;
import edu.emory.cci.bindaas.core.api.IValidator;
import edu.emory.cci.bindaas.core.exception.ValidationException;
import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.api.ISubmitPayloadModifier;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;

public class ValidatorImpl implements IValidator{
	private IModifierRegistry modifierRegistry;
	
	public IModifierRegistry getModifierRegistry() {
		return modifierRegistry;
	}

	public void setModifierRegistry(IModifierRegistry modifierRegistry) {
		this.modifierRegistry = modifierRegistry;
	}

	@Override
	public void validateQueryModifierRequestChain(
			ModifierEntry queryModifierChain) throws ValidationException {
		
		ModifierEntry next = queryModifierChain;
		while(next!=null)
		{
			IQueryModifier queryModifier = modifierRegistry.findQueryModifier(next.getName()) ;
			if(queryModifier!=null)
			{
				next = next.getAttachment();
			}
			else
			{
				throw new ValidationException("[IQueryModifier] by id=[" + next.getName()  +"] not found");
			}
			
		}
		
		
	}

	@Override
	public void validateQueryResultModifierRequestChain(
			ModifierEntry queryResultModifierChain) throws ValidationException {
		ModifierEntry next = queryResultModifierChain;
		while(next!=null)
		{
			IQueryResultModifier queryResultModifier = modifierRegistry.findQueryResultModifier(next.getName()) ;
			if(queryResultModifier!=null)
			{
				next = next.getAttachment();
			}
			else
			{
				throw new ValidationException("[IQueryResultModifier] by id=[" + next.getName()  +"] not found");
			}
			
		}
		
	}

	@Override
	public void validateSubmitPayloadModifierRequestChain(
			ModifierEntry submitModifierChain) throws ValidationException {
		ModifierEntry next = submitModifierChain;
		while(next!=null)
		{
			ISubmitPayloadModifier submitPayloadModifier = modifierRegistry.findSubmitPayloadModifier(next.getName()) ;
			if(submitPayloadModifier!=null)
			{
				next = next.getAttachment();
			}
			else
			{
				throw new ValidationException("[ISubmitPayloadModifier] by id=[" + next.getName()  +"] not found");
			}
			
		}
		
	}
	

}
