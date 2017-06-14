package edu.emory.cci.bindaas.junit.mock;

import java.io.ByteArrayInputStream;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public class MockQueryHandler implements IQueryHandler{

	@Override
	public QueryResult query(JsonObject dataSource,JsonObject outputFormatProps, String queryToExecute, Map<String,String> runtimeParameters, RequestContext requestContext)
			throws AbstractHttpCodeException {
		QueryResult result = new QueryResult();
		
		result.setError(false);
		result.setMimeType("text");
		JsonObject res = new JsonObject();
		res.add("query", new JsonPrimitive(queryToExecute));
		result.setData(new ByteArrayInputStream(res.toString().getBytes()));
		return result;
	}

	@Override
	public QueryEndpoint validateAndInitializeQueryEndpoint(
			QueryEndpoint queryEndpoint) throws ProviderException {
		queryEndpoint.setDescription(queryEndpoint.getDescription() + "verified");
		return queryEndpoint;
	}

	@Override
	public JsonObject getOutputFormatSchema() {
		return new JsonObject();
	}

}
