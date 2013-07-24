package edu.emory.cci.bindaas.framework.api;

import java.util.Map;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public interface IQueryHandler {

	public QueryResult query(JsonObject dataSource , JsonObject outputFormatProps ,String queryToExecute , Map<String,String> runtimeParameters , RequestContext requestContext) throws AbstractHttpCodeException;
	
	/**
	 * Validate the correctness of QueryEndpoint params - outputformat , etc.
	 * @param queryEndpoint
	 * @return
	 * @throws ProviderException
	 */
	public QueryEndpoint validateAndInitializeQueryEndpoint(QueryEndpoint queryEndpoint) throws ProviderException;
	public JsonObject getOutputFormatSchema() ;
	
}
