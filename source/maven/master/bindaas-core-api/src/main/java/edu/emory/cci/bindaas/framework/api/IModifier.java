package edu.emory.cci.bindaas.framework.api;

import com.google.gson.JsonObject;

public interface IModifier {
	public String getInletType();
	public String getOutletType();
	public JsonObject getDocumentation();
	

}
