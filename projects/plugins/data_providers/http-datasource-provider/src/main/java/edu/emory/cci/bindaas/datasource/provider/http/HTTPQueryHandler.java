package edu.emory.cci.bindaas.datasource.provider.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.http.model.HTTPQuery;
import edu.emory.cci.bindaas.framework.api.IQueryHandler;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryEndpoint;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.QueryResult.Callback;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.NetworkConnectionException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;

public class HTTPQueryHandler implements IQueryHandler {
	
	private Log log = LogFactory.getLog(getClass());

	@Override
	public QueryResult query(JsonObject dataSource,
			JsonObject outputFormatProps, String queryToExecute, Map<String,String> runtimeParameters, RequestContext requestContext)
			throws AbstractHttpCodeException {
		
		
		try {
			
			HTTPQuery httpQuery = GSONUtil.getGSONInstance().fromJson(queryToExecute, HTTPQuery.class);
			final HttpResponse response = httpQuery.execute();
			
			if(response!=null && response.getStatusLine().getStatusCode() == 200 && response.getEntity()!=null)
			{
				final QueryResult result = new QueryResult();
				
				result.setCallback(new Callback() {
					
					@Override
					public void callback(OutputStream servletOutputStream, Properties context)
							throws AbstractHttpCodeException {

						try {
							response.getEntity().writeTo(servletOutputStream);
						} catch (IOException e) {
							log.error(e);
							throw new NetworkConnectionException(getClass().getName(), 1 , e);
						}
					}
				});
				
				result.setMimeType(response.getFirstHeader("Content-Type").getValue());
				return result;
				
			}
			else
			{
				throw new Exception("Error connecting remote URL [" + httpQuery.getUrl() + "]. Server Response [" + response.getStatusLine().toString() + "]" );

			}
		
		} catch (Exception e) {
			log.error(e);
			throw new QueryExecutionFailedException(HTTPProvider.class.getName(), HTTPProvider.VERSION);
		}
		
	}

	@Override
	public QueryEndpoint validateAndInitializeQueryEndpoint(
			QueryEndpoint queryEndpoint) throws ProviderException {
		String template = queryEndpoint.getQueryTemplate();
		HTTPQuery httpQuery = GSONUtil.getGSONInstance().fromJson(template, HTTPQuery.class);
		
		if(httpQuery !=null)
			try {
				httpQuery.validate();
			} catch (Exception e) {
				throw new ProviderException(HTTPProvider.class.getName(), HTTPProvider.VERSION , e);
			}
		else
			throw new ProviderException(HTTPProvider.class.getName(), HTTPProvider.VERSION);
		queryEndpoint.setOutputFormat(new JsonObject());
		return queryEndpoint;
		
	}

	@Override
	public JsonObject getOutputFormatSchema() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
