package edu.emory.cci.bindaas.core.api;

import java.util.Map;

import edu.emory.cci.bindaas.core.exception.ValidationException;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;

public interface IValidator {

	
	public void validateQueryModifierRequestChain(Map<Integer,ModifierEntry> queryModifierChain) throws ValidationException;
	public void validateQueryResultModifierRequestChain(Map<Integer,ModifierEntry> queryModifierChain) throws ValidationException;
	public void validateSubmitPayloadModifierRequestChain(Map<Integer,ModifierEntry> queryModifierChain) throws ValidationException;
	
	
}
