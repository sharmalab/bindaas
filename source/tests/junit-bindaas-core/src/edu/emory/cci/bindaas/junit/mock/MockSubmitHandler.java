package edu.emory.cci.bindaas.junit.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint.Type;

public class MockSubmitHandler implements ISubmitHandler {

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, InputStream is, RequestContext requestContext)
			throws ProviderException {
		
		QueryResult result = new QueryResult();
		
		result.setError(false);
		result.setMimeType("text");
		try {
			result.setData(is);
		} catch (RuntimeException e) {
			throw new ProviderException(MockProvider.class.getName() , MockProvider.VERSION , e);
		}
		return result;
	}

	@Override
	public QueryResult submit(JsonObject dataSource,
			JsonObject endpointProperties, String data, RequestContext requestContext)
			throws ProviderException {
		QueryResult result = new QueryResult();
		
		result.setError(false);
		result.setMimeType("text");
		result.setData(new ByteArrayInputStream(data.getBytes()));
		return result;
	}

	@Override
	public SubmitEndpoint validateAndInitializeSubmitEndpoint(
			SubmitEndpoint submitEndpoint) throws ProviderException {
		
		submitEndpoint.setType(Type.FORM_DATA); // TODO : Flip this to swtich between FORM_DATA and MIME type
		submitEndpoint.setType(Type.MULTIPART); // TODO : Flip this to swtich between FORM_DATA and MULTIPART type
		return submitEndpoint;
	}

	@Override
	public JsonObject getSubmitPropertiesSchema() {

		return new JsonObject();
	}

}
