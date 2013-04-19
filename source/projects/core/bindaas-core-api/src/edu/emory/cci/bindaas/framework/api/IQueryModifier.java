package edu.emory.cci.bindaas.framework.api;

import java.util.Map;

import com.google.gson.JsonObject;

public interface IQueryModifier extends IModifier {

	public String modifyQuery(String query , JsonObject dataSource , String user , JsonObject modifierProperties) throws Exception;
	public Map<String,String> modiftQueryParameters(Map<String,String> queryParams , JsonObject dataSource , String user , JsonObject modifierProperties) throws Exception;
}
