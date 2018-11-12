package edu.emory.cci.sample.modifiers;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.IQueryModifier;
import edu.emory.cci.bindaas.framework.model.ModifierException;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;

public class LoggingQM implements IQueryModifier{

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

		return "Plugin for logging query";
	}

	@Override
	public String modifyQuery(String query, JsonObject dataSource,
			RequestContext requestContext, JsonObject modifierProperties)
			throws AbstractHttpCodeException {
		log.info("Query  intercepted [" + query + "]");
		return query;
	}

	@Override
	public Map<String, String> modiftQueryParameters(
			Map<String, String> queryParams, JsonObject dataSource,
			RequestContext requestContext, JsonObject modifierProperties)
			throws AbstractHttpCodeException {
		
		return queryParams;
	}

	
}
