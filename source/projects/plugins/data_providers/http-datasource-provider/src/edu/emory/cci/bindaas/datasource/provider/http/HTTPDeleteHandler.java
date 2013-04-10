package edu.emory.cci.bindaas.datasource.provider.http;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.IDeleteHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;

public class HTTPDeleteHandler implements IDeleteHandler {

	@Override
	public QueryResult delete(JsonObject dataSource, String deleteQueryToExecute)
			throws ProviderException {
		throw new ProviderException(HTTPProvider.class.getName(), HTTPProvider.VERSION, "Method not implemented");
	}

}
