package edu.emory.cci.bindaas.framework.model;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class SubmitEndpoint extends Entity {

	public enum Type {
		FORM_DATA , MULTIPART
	}
	
	
	@Expose private Type type;
	@Expose private JsonObject properties;
	@Expose private ModifierEntry submitPayloadModifiers;
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public JsonObject getProperties() {
		return properties;
	}
	public void setProperties(JsonObject properties) {
		this.properties = properties;
	}
	public ModifierEntry getSubmitPayloadModifiers() {
		return submitPayloadModifiers;
	}
	public void setSubmitPayloadModifiers(ModifierEntry submitPayloadModifiers) {
		this.submitPayloadModifiers = submitPayloadModifiers;
	}
	
	
	
}
