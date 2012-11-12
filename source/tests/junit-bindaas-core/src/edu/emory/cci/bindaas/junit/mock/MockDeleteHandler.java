package edu.emory.cci.bindaas.junit.mock;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public class MockDeleteHandler implements IDeleteHandler{

	@Override
	public QueryResult delete(JsonObject dataSource, String deleteQueryToExecute)
			throws ProviderException {
		QueryResult result = new QueryResult();
		result.setCallback(false);
		result.setError(false);
		result.setMimeType("text");
		JsonObject res = new JsonObject();
		res.add("query", new JsonPrimitive(deleteQueryToExecute));
		result.setData(res.toString().getBytes());
		return result;
	}

}
