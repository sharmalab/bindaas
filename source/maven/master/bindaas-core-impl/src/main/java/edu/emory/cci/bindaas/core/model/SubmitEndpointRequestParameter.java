package edu.emory.cci.bindaas.core.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.model.ModifierEntry;

public class SubmitEndpointRequestParameter {

	public ModifierEntry getSubmitPayloadModifiers() {
		return submitPayloadModifiers;
	}
	public void setSubmitPayloadModifiers(ModifierEntry submitPayloadModifiers) {
		this.submitPayloadModifiers = submitPayloadModifiers;
	}
	@Expose private JsonObject properties;
	@Expose private ModifierEntry submitPayloadModifiers;
	public JsonObject getProperties() {
		return properties;
	}
	public void setProperties(JsonObject properties) {
		this.properties = properties;
	}
	
}
