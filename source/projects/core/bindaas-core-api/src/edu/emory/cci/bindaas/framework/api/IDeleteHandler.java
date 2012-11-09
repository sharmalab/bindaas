package edu.emory.cci.bindaas.framework.api;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public interface IDeleteHandler {

	public QueryResult delete(JsonObject dataSource , String deleteQueryToExecute) throws ProviderException;
}
