package edu.emory.cci.bindaas.framework.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class SubmitEndpoint extends Entity {

	public enum Type {
		FORM_DATA , MULTIPART
	}
	
	@Override
	public void validate() throws Exception {
		super.validate();
		
		if(type == null)
			throw new Exception("Type field cannot be null");
		if(properties == null)
			properties = new JsonObject();
		
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
