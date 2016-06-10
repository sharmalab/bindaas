package edu.emory.cci.bindaas.core.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.framework.model.ModifierEntry;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;

public class SubmitEndpointRequestParameter {

	public ModifierEntry getSubmitPayloadModifiers() {
		return submitPayloadModifiers;
	}
	public void setSubmitPayloadModifiers(ModifierEntry submitPayloadModifiers) {
		this.submitPayloadModifiers = submitPayloadModifiers;
	}
	@Expose private JsonObject properties;
	@Expose private ModifierEntry submitPayloadModifiers;
	@Expose private String description;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public JsonObject getProperties() {
		return properties;
	}
	public void setProperties(JsonObject properties) {
		this.properties = properties;
	}
	
	public SubmitEndpoint getSubmitEndpoint(SubmitEndpoint submitEndpoint) throws Exception
	{
		submitEndpoint.setProperties(getProperties());
		submitEndpoint.setSubmitPayloadModifiers(getSubmitPayloadModifiers());
		submitEndpoint.setDescription(getDescription());
		return submitEndpoint;
	}
	public static SubmitEndpointRequestParameter convert(
			SubmitEndpoint submitEndpoint) {
		SubmitEndpointRequestParameter serp = new SubmitEndpointRequestParameter();
		serp.setProperties(submitEndpoint.getProperties());
		serp.setSubmitPayloadModifiers(submitEndpoint.getSubmitPayloadModifiers());
		serp.setDescription(submitEndpoint.getDescription());
		return serp;
	}
}
