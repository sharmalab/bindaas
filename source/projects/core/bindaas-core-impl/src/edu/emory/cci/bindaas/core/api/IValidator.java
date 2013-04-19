package edu.emory.cci.bindaas.core.api;

import edu.emory.cci.bindaas.core.exception.ValidationException;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;

public interface IValidator {

	
	public void validateQueryModifierRequestChain(ModifierEntry queryModifierChain) throws ValidationException;
	public void validateQueryResultModifierRequestChain(ModifierEntry queryModifierChain) throws ValidationException;
	public void validateSubmitPayloadModifierRequestChain(ModifierEntry queryModifierChain) throws ValidationException;
	
	
}
