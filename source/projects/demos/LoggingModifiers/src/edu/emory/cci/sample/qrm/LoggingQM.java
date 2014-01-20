package edu.emory.cci.sample.qrm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.IQueryResultModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;

public class LoggingQM implements IQueryResultModifier{

	private Log log = LogFactory.getLog(getClass());
	@Override
	public JsonObject getDocumentation() {

		return new JsonObject();
	}

	@Override
	public void validate() throws ModifierException {
		// not implemented
		
	}

	@Override
	public String getDescriptiveName() {

		return "Plugin for logging query results";
	}

	@Override
	public QueryResult modifyQueryResult(QueryResult queryResult,
			JsonObject dataSource, RequestContext requestContext,
			JsonObject modifierProperties, Map<String, String> queryParams)
			throws AbstractHttpCodeException {
		String data;
		try {
			
			// intercepting the response and logging it
			data = IOUtils.toString(queryResult.getData());
			log.info("Request received from [" + requestContext.getUser() + "]");
			log.info("Response [" + data + "]");
			// overriding the response by a custom message
			queryResult.setMimeType(StandardMimeType.TEXT.toString());
			queryResult.setData(new ByteArrayInputStream(new String("Response from the QueryHandler was consumed by LoggingQRM").getBytes()));
			return queryResult;
		} catch (IOException e) {
			log.error("Execution of LoggingQRM failed");
			throw new QueryExecutionFailedException(getClass().getName(), 1);
		}

	}

}
