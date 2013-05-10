package edu.emory.cci.bindaas.datasource.provider.http;

import java.util.Map;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;

public class HTTPDeleteHandler implements IDeleteHandler {

	@Override
	public QueryResult delete(JsonObject dataSource, String deleteQueryToExecute ,Map<String,String> runtimeParamters , RequestContext requestContext)
			throws ProviderException {
		throw new ProviderException(HTTPProvider.class.getName(), HTTPProvider.VERSION, "Method not implemented");
	}

}
