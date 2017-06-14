package edu.emory.cci.bindaas.framework.api;

import java.util.Map;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public interface IQueryModifier extends IModifier {

	public String modifyQuery(String query , JsonObject dataSource , RequestContext requestContext, JsonObject modifierProperties) throws AbstractHttpCodeException;
	public Map<String,String> modiftQueryParameters(Map<String,String> queryParams , JsonObject dataSource , RequestContext requestContext , JsonObject modifierProperties) throws AbstractHttpCodeException;
}
