package edu.emory.cci.bindaas.junit.mock;

import java.io.ByteArrayInputStream;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public class MockDeleteHandler implements IDeleteHandler{

	@Override
	public QueryResult delete(JsonObject dataSource, String deleteQueryToExecute , Map<String,String> runtimeParamters , RequestContext requestContext)
			throws AbstractHttpCodeException {
		QueryResult result = new QueryResult();
		
		result.setError(false);
		result.setMimeType("text");
		JsonObject res = new JsonObject();
		res.add("query", new JsonPrimitive(deleteQueryToExecute));
		result.setData(new ByteArrayInputStream(res.toString().getBytes()));
		return result;
	}

}
