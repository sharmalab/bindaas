package edu.emory.cci.bindaas.framework.api;

import com.google.gson.JsonObject;

public interface IQueryModifier extends IModifier {

	public String modifyQuery(String query , JsonObject dataSource , String user , JsonObject modifierProperties) throws Exception;
	
}
