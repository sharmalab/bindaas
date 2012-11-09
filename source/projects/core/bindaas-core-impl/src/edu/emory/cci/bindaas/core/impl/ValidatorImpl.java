package edu.emory.cci.bindaas.core.impl;

import java.util.Map;

import edu.emory.cci.bindaas.core.api.IValidator;
import edu.emory.cci.bindaas.core.exception.ValidationException;
import edu.emory.cci.bindaas.framework.model.ModifierEntry;

public class ValidatorImpl implements IValidator{

	@Override
	public void validateQueryModifierRequestChain(
			Map<Integer, ModifierEntry> queryModifierChain)
			throws ValidationException {
		// TODO implement later
		
	}

	@Override
	public void validateQueryResultModifierRequestChain(
			Map<Integer, ModifierEntry> queryModifierChain)
			throws ValidationException {
		// TODO implement later
		
	}

	@Override
	public void validateSubmitPayloadModifierRequestChain(
			Map<Integer, ModifierEntry> queryModifierChain)
			throws ValidationException {
		// TODO implement later
		
	}

}
