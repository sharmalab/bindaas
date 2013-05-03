package edu.emory.cci.bindaas.framework.api;

import java.util.Map;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public interface IQueryHandler {

	public QueryResult query(JsonObject dataSource , JsonObject outputFormatProps ,String queryToExecute , Map<String,String> runtimeParameters) throws ProviderException;
	
	/**
	 * Validate the correctness of QueryEndpoint params - outputformat , etc.
	 * @param queryEndpoint
	 * @return
	 * @throws ProviderException
	 */
	public QueryEndpoint validateAndInitializeQueryEndpoint(QueryEndpoint queryEndpoint) throws ProviderException;
	public JsonObject getOutputFormatSchema() ;
	
}
