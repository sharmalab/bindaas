package edu.emory.cci.bindaas.framework.api;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.ModifierException;

public interface IModifier {
	
	public JsonObject getDocumentation();
	public void validate() throws ModifierException;
	public String getDescriptiveName();
}
