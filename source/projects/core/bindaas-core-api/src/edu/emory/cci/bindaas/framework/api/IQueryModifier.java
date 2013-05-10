package edu.emory.cci.bindaas.framework.api;

import java.util.Map;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.RequestContext;

public interface IQueryModifier extends IModifier {

	public String modifyQuery(String query , JsonObject dataSource , RequestContext requestContext, JsonObject modifierProperties) throws Exception;
	public Map<String,String> modiftQueryParameters(Map<String,String> queryParams , JsonObject dataSource , RequestContext requestContext , JsonObject modifierProperties) throws Exception;
}
