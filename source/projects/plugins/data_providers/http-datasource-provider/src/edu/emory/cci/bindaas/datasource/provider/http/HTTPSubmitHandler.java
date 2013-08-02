package edu.emory.cci.bindaas.datasource.provider.http;

import java.io.InputStream;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.MethodNotImplementedException;

public class HTTPSubmitHandler implements ISubmitHandler {

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, InputStream is, RequestContext requestContext)
			throws AbstractHttpCodeException {
		throw new MethodNotImplementedException(HTTPProvider.class.getName(), HTTPProvider.VERSION, "Method not implemented");
	}

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, String data, RequestContext requestContext)
			throws AbstractHttpCodeException {
		throw new MethodNotImplementedException(HTTPProvider.class.getName(), HTTPProvider.VERSION, "Method not implemented");
	}

	@Override
	public SubmitEndpoint validateAndInitializeSubmitEndpoint(
			SubmitEndpoint submitEndpoint) throws AbstractHttpCodeException {
		throw new MethodNotImplementedException(HTTPProvider.class.getName(), HTTPProvider.VERSION, "Method not implemented");
	}

	@Override
	public JsonObject getSubmitPropertiesSchema() {
		
		throw new RuntimeException("Method not implemented");
	}

}
