package edu.emory.cci.bindaas.framework.api;

import java.io.InputStream;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public interface ISubmitHandler {

	public QueryResult submit(JsonObject dataSource , JsonObject endpointProperties , InputStream is , RequestContext requestContext) throws AbstractHttpCodeException;
	public QueryResult submit(JsonObject dataSource , JsonObject endpointProperties , String data , RequestContext requestContext) throws AbstractHttpCodeException;
	
	/**
	 * Must set Type attribute for the endpoint
	 * @param submitEndpoint
	 * @return
	 * @throws ProviderException
	 */
	public SubmitEndpoint validateAndInitializeSubmitEndpoint(SubmitEndpoint submitEndpoint) throws ProviderException;
	public JsonObject getSubmitPropertiesSchema();
}

