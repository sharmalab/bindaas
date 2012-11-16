package edu.emory.cci.bindaas.framework.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class ModifierEntry {

	@Expose private String name;
	@Expose private JsonObject properties;
	@Expose private ModifierEntry attachment;
	
	public ModifierEntry getAttachment() {
		return attachment;
	}
	public void setAttachment(ModifierEntry attachment) {
		this.attachment = attachment;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public JsonObject getProperties() {
		return properties;
	}
	public void setProperties(JsonObject properties) {
		this.properties = properties;
	}
}
