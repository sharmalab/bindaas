package edu.emory.cci.bindaas.framework.api;

import java.util.Map;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;

public interface IDeleteHandler {

	public QueryResult delete(JsonObject dataSource , String deleteQueryToExecute , Map<String, String> runtimeParameters , RequestContext requestContext) throws ProviderException;
}
